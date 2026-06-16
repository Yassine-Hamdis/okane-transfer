package com.okanetransfer.service;

import com.okanetransfer.dto.request.CancelTransferRequest;
import com.okanetransfer.dto.request.CreateTransferRequest;
import com.okanetransfer.dto.request.FeeSimulationRequest;
import com.okanetransfer.dto.request.PayoutRequest;
import com.okanetransfer.dto.response.FeeSimulationResponse;
import com.okanetransfer.dto.response.TransferResponse;
import com.okanetransfer.entity.*;
import com.okanetransfer.entity.enums.OperationType;
import com.okanetransfer.entity.enums.Role;
import com.okanetransfer.entity.enums.TransferStatus;
import com.okanetransfer.entity.enums.TransferType;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.*;
import com.okanetransfer.util.AesEncryptionUtil;
import com.okanetransfer.util.CredentialsGenerator;
import com.okanetransfer.util.WithdrawalCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.okanetransfer.entity.ExchangeRate;
import java.math.RoundingMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    // Amount above which admin approval is required
    private static final BigDecimal APPROVAL_THRESHOLD = new BigDecimal("10000");

    @Autowired private TransferRepository      transferRepository;
    @Autowired private AgencyRepository        agencyRepository;
    @Autowired private UserRepository          userRepository;
    @Autowired private CorridorRepository      corridorRepository;
    @Autowired private CurrencyRepository      currencyRepository;
    @Autowired private CashRegisterRepository  cashRegisterRepository;
    @Autowired private CashRegisterBalanceRepository cashRegisterBalanceRepository;
    @Autowired private CashOperationRepository cashOperationRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private ExchangeRateRepository  exchangeRateRepository; // ← ADD THIS

    @Autowired private FeeGridService          feeGridService;
    @Autowired private KycService              kycService;
    @Autowired private NotificationService     notificationService;
    @Autowired private AuditService            auditService;

    @Autowired private AesEncryptionUtil       aesUtil;
    @Autowired private WithdrawalCodeGenerator codeGenerator;
    @Autowired private CredentialsGenerator    credentialsGenerator;

    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // ─────────────────────────────────────────────────────
    //  CREATE TRANSFER
    // ─────────────────────────────────────────────────────

    @Transactional
    public TransferResponse createTransfer(CreateTransferRequest request,
                                           Long agentId) {

        // ── 1. Load & validate agent ──────────────────
        User agent = findUser(agentId);
        Agency sendingAgency = agent.getAgency();
        if (sendingAgency == null || !sendingAgency.isActive()) {
            throw new IllegalStateException("Agent has no active agency assigned");
        }

        // ── 2. Load & validate corridor ───────────────
        Corridor corridor = corridorRepository.findById(request.getCorridorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Corridor", request.getCorridorId()));

        if (!corridor.isActive()) {
            throw new IllegalArgumentException("Corridor is inactive");
        }

        // ── 3. Load countries & currency ─────────────
        Country senderCountry = findCountry(request.getSenderCountryId());
        Country recipientCountry = findCountry(request.getRecipientCountryId());
        Currency sentCurrency = findCurrency(request.getSentCurrencyId());

        // ── 4. Get transfer type ──────────────────────
        TransferType transferType = request.getTransferType() != null
                ? request.getTransferType()
                : TransferType.STANDARD;

        // ── 5. Handle MOBILE_MONEY vs STANDARD/EXPRESS ───
        FeeSimulationResponse sim;

        if (transferType == TransferType.MOBILE_MONEY) {
            // Mobile Money: bypass fee grid, use direct calculation
            sim = calculateMobileMoneyTransfer(
                    corridor,
                    sentCurrency,
                    request.getSentAmount()
            );
        } else {
            // Standard/Express: use fee grid simulation
            FeeSimulationRequest simRequest = new FeeSimulationRequest(
                    corridor.getId(),
                    sentCurrency.getId(),
                    request.getSentAmount(),
                    transferType
            );
            sim = feeGridService.simulateFee(simRequest);
        }
        // ── 6. Check agency daily limit ───────────────
        checkDailyLimit(sendingAgency, request.getSentAmount());

        // ── 7. Encrypt sender ID ──────────────────────
        String senderIdEncrypted = null;
        if (request.getSenderIdNumber() != null &&
                !request.getSenderIdNumber().isBlank()) {
            senderIdEncrypted = aesUtil.encrypt(request.getSenderIdNumber());
        }

// ── 8. Generate withdrawal code (skip for MOBILE_MONEY) ────
        String withdrawalCode;
        if (transferType == TransferType.MOBILE_MONEY) {
            // Mobile money doesn't need withdrawal code
            withdrawalCode = null;  // or "DIRECT_TRANSFER"
        } else {
            withdrawalCode = codeGenerator.generate().getCode();
        }
        // ── 9. Check admin approval threshold ─────────
        boolean requiresApproval =
                request.getSentAmount().compareTo(APPROVAL_THRESHOLD) >= 0;

        // ── 10. Resolve or auto-create client account ──
        User client = resolveClientAccount(
                request.getSenderFirstName(),
                request.getSenderLastName(),
                request.getSenderPhone(),
                request.getSenderEmail()
        );

        // ── 11. Build & save transfer ─────────────────
        Transfer transfer = Transfer.builder()
                .withdrawalCode(withdrawalCode)
                .senderFirstName(request.getSenderFirstName())
                .senderLastName(request.getSenderLastName())
                .senderPhone(request.getSenderPhone())
                .senderIdEncrypted(senderIdEncrypted)
                .senderCountry(senderCountry)
                .recipientFirstName(request.getRecipientFirstName())
                .recipientLastName(request.getRecipientLastName())
                .recipientPhone(request.getRecipientPhone())
                .recipientIdEncrypted(null)
                .recipientCountry(recipientCountry)
                .sentAmount(request.getSentAmount())
                .sentCurrency(sentCurrency)
                .feeFixed(sim.getFeeFixedAmount())
                .feePercentage(sim.getFeePercentage())
                .feeAmount(sim.getFeeAmount())
                .receivedAmount(sim.getReceivedAmount())
                .receivedCurrency(currencyRepository
                        .findByCode(sim.getReceivedCurrency())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Currency " + sim.getReceivedCurrency())))
                .exchangeRate(sim.getExchangeRate())
                .transferType(transferType)
                .status(TransferStatus.EN_ATTENTE)
                .requiresAdminApproval(requiresApproval)
                .corridor(corridor)
                .sendingAgency(sendingAgency)
                .receivingAgency(null)
                .sendingAgent(agent)
                .receivingAgent(null)
                .client(client)
                .notes(request.getNotes())
                .build();

        Transfer saved = transferRepository.save(transfer);

        // ── 12. Cash operation — ENVOI ────────────────
        recordCashOperation(
                sendingAgency,
                OperationType.ENVOI,
                request.getSentAmount(),
                sentCurrency,
                agent,
                saved
        );

        // ── 13. KYC auto-check ────────────────────────
        kycService.autoCheck(saved);

        // ── 14. Notify client ─────────────────────────
        if (client != null) {
            notificationService.notifyTransferCreated(
                    client.getId(),
                    saved.getId(),
                    withdrawalCode,
                    request.getRecipientFirstName() + " " + request.getRecipientLastName(),
                    request.getSentAmount().toPlainString(),
                    sentCurrency.getCode(),
                    client.getEmail()
            );
        }

        // ── 15. Audit ─────────────────────────────────
        auditService.log(agentId, "TRANSFER_CREATED",
                "Transfer", saved.getId(),
                "{\"code\":\"" + withdrawalCode +
                        "\",\"amount\":" + request.getSentAmount() +
                        "\",\"type\":\"" + transferType + "\"}");

        return toResponse(saved);
    }

    /**
     * Calculate Mobile Money transfer without fee grid.
     * Uses current exchange rate from corridor.
     * No fees applied for mobile money transfers.
     */
    private FeeSimulationResponse calculateMobileMoneyTransfer(
            Corridor corridor,
            Currency sentCurrency,
            BigDecimal sentAmount) {

        // Get current exchange rate
        ExchangeRate currentRate = exchangeRateRepository
                .findByCorridorIdAndIsCurrentTrue(corridor.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Current exchange rate not found for corridor: " + corridor.getId()));

        // Mobile Money: zero fees (sent directly to wallet)
        BigDecimal feeFixed = BigDecimal.ZERO;
        BigDecimal feePercentage = BigDecimal.ZERO;
        BigDecimal feeAmount = BigDecimal.ZERO;

        // Full amount goes to recipient (no fees deducted)
        BigDecimal amountAfterFee = sentAmount;
        BigDecimal receivedAmount = amountAfterFee
                .multiply(currentRate.getRate())
                .setScale(2, RoundingMode.HALF_UP);

        return FeeSimulationResponse.builder()
                .feeGridId(null)  // No fee grid for mobile money
                .sentAmount(sentAmount)
                .sentCurrency(sentCurrency.getCode())
                .feeFixedAmount(feeFixed)
                .feePercentage(feePercentage)
                .feeAmount(feeAmount)
                .amountAfterFee(amountAfterFee)
                .exchangeRate(currentRate.getRate())
                .receivedAmount(receivedAmount)
                .receivedCurrency(corridor.getDestinationCurrency().getCode())
                .agencyShare(BigDecimal.ZERO)
                .centralShare(BigDecimal.ZERO)
                .transferType(TransferType.MOBILE_MONEY)
                .build();
    }
    // ─────────────────────────────────────────────────────
    //  PAYOUT (RETRAIT)
    // ─────────────────────────────────────────────────────

    @Transactional
    public TransferResponse processPayment(PayoutRequest request, Long agentId) {

        // ── 1. Load agent & agency ────────────────────
        User agent = findUser(agentId);
        Agency receivingAgency = agent.getAgency();
        if (receivingAgency == null || !receivingAgency.isActive()) {
            throw new IllegalStateException("Agent has no active agency assigned");
        }

        // ── 2. Find transfer by code ──────────────────
        Transfer transfer = transferRepository
                .findByWithdrawalCode(request.getWithdrawalCode().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transfer not found with code: " + request.getWithdrawalCode()));

        // ── 3. Validate status ────────────────────────
        if (transfer.getStatus() != TransferStatus.EN_ATTENTE) {
            throw new IllegalStateException(
                    "Transfer cannot be paid. Current status: " + transfer.getStatus());
        }

        // ── 4. Check not expired ──────────────────────
        if (LocalDateTime.now().isAfter(transfer.getExpiresAt())) {
            transfer.setStatus(TransferStatus.EXPIRE);
            transferRepository.save(transfer);
            throw new IllegalStateException("Transfer has expired");
        }

        // ── 5. Check not blocked ──────────────────────
        if (transfer.isRequiresAdminApproval()) {
            throw new IllegalStateException(
                    "Transfer is pending admin approval and cannot be paid out yet");
        }

        // ── 6. Encrypt recipient ID ───────────────────
        String recipientIdEncrypted = aesUtil.encrypt(request.getRecipientIdNumber());

        // ── 7. Update transfer ────────────────────────
        transfer.setRecipientIdEncrypted(recipientIdEncrypted);
        transfer.setReceivingAgency(receivingAgency);
        transfer.setReceivingAgent(agent);
        transfer.setStatus(TransferStatus.PAYE);
        transfer.setPaidAt(LocalDateTime.now());

        Transfer saved = transferRepository.save(transfer);

        // ── 8. Cash operation — RETRAIT ───────────────
        recordCashOperation(
                receivingAgency,
                OperationType.RETRAIT,
                transfer.getReceivedAmount(),
                transfer.getReceivedCurrency(),
                agent,
                saved
        );

        // ── 9. Notify client ──────────────────────────
        if (transfer.getClient() != null) {
            notificationService.notifyTransferPaid(
                    transfer.getClient().getId(),
                    saved.getId(),
                    transfer.getReceivedAmount().toPlainString(),
                    transfer.getReceivedCurrency().getCode(),
                    transfer.getClient().getEmail()
            );
        }

        // ── 10. Audit ──────────────────────────────────
        auditService.log(agentId, "PAYOUT_CONFIRMED",
                "Transfer", saved.getId(),
                "{\"code\":\"" + request.getWithdrawalCode() + "\"}");

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────
    //  CANCEL
    // ─────────────────────────────────────────────────────

    @Transactional
    public TransferResponse cancelTransfer(Long transferId,
                                           CancelTransferRequest request,
                                           Long userId) {
        Transfer transfer = findTransfer(transferId);

        if (transfer.getStatus() != TransferStatus.EN_ATTENTE) {
            throw new IllegalStateException(
                    "Only EN_ATTENTE transfers can be cancelled. " +
                            "Current: " + transfer.getStatus());
        }

        transfer.setStatus(TransferStatus.ANNULE);
        transfer.setCancellationReason(request.getReason());

        Transfer saved = transferRepository.save(transfer);

        // Cash operation — ANNULATION (reverse the ENVOI)
        recordCashOperation(
                saved.getSendingAgency(),
                OperationType.ANNULATION,
                saved.getSentAmount(),
                saved.getSentCurrency(),
                findUser(userId),
                saved
        );

        // Notify client
        if (saved.getClient() != null) {
            notificationService.notifyTransferCancelled(
                    saved.getClient().getId(),
                    saved.getId(),
                    request.getReason(),
                    saved.getClient().getEmail()
            );
        }

        auditService.log(userId, "TRANSFER_CANCELLED",
                "Transfer", transferId,
                "{\"reason\":\"" + request.getReason() + "\"}");

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────
    //  ADMIN — APPROVE BLOCKED TRANSFER
    // ─────────────────────────────────────────────────────

    @Transactional
    public TransferResponse approveTransfer(Long transferId, Long adminId) {
        Transfer transfer = findTransfer(transferId);

        if (!transfer.isRequiresAdminApproval()) {
            throw new IllegalStateException("Transfer does not require approval");
        }

        transfer.setRequiresAdminApproval(false);
        transfer.setBlockedReason(null);
        Transfer saved = transferRepository.save(transfer);

        auditService.log(adminId, "TRANSFER_APPROVED",
                "Transfer", transferId, null);

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────
    //  QUERIES
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TransferResponse getById(Long id) {
        return toResponse(findTransfer(id));
    }

    @Transactional(readOnly = true)
    public TransferResponse getByWithdrawalCode(String code) {
        Transfer transfer = transferRepository
                .findByWithdrawalCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transfer not found with code: " + code));
        return toResponse(transfer);
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> getAll() {
        return transferRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> getByStatus(TransferStatus status) {
        return transferRepository.findAllByStatus(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> getMyTransfersAsAgent(Long agentId) {
        return transferRepository.findAllBySendingAgentId(agentId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> getByAgency(Long agencyId) {
        return transferRepository.findAllBySendingAgencyId(agencyId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> searchByRecipientPhone(String phone) {
        return transferRepository.findAllByRecipientPhone(phone)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────
    //  SCHEDULED — Auto-expire transfers
    // ─────────────────────────────────────────────────────

    /**
     * Runs every hour.
     * Finds all EN_ATTENTE transfers past their expiry date
     * and marks them as EXPIRE.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void expireOldTransfers() {
        List<Transfer> expired = transferRepository
                .findAllByStatusAndExpiresAtBefore(
                        TransferStatus.EN_ATTENTE,
                        LocalDateTime.now()
                );

        if (expired.isEmpty()) return;

        expired.forEach(t -> t.setStatus(TransferStatus.EXPIRE));
        transferRepository.saveAll(expired);

        log.info("[SCHEDULER] Expired {} transfers", expired.size());
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE — Cash helpers
    // ─────────────────────────────────────────────────────

    private void recordCashOperation(Agency agency,
                                     OperationType type,
                                     BigDecimal amount,
                                     Currency currency,
                                     User agent,
                                     Transfer transfer) {
        CashRegister cashRegister = cashRegisterRepository
                .findByAgencyId(agency.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "No cash register found for agency: " + agency.getId()));

        CashRegisterBalance balance = cashRegisterBalanceRepository
                .findByCashRegisterIdAndCurrencyId(
                        cashRegister.getId(), currency.getId())
                .orElseGet(() -> CashRegisterBalance.builder()
                        .cashRegister(cashRegister)
                        .currency(currency)
                        .currentBalance(BigDecimal.ZERO)
                        .build());

        BigDecimal newBalance = switch (type) {
            case ENVOI      -> balance.getCurrentBalance().add(amount);
            case RETRAIT    -> balance.getCurrentBalance().subtract(amount);
            case ANNULATION -> balance.getCurrentBalance().subtract(amount);
            default         -> balance.getCurrentBalance();
        };

        balance.setCurrentBalance(newBalance);
        cashRegisterBalanceRepository.save(balance);

        CashOperation operation = CashOperation.builder()
                .cashRegister(cashRegister)
                .type(type)
                .amount(amount)
                .balanceAfter(newBalance)
                .currency(currency)
                .agent(agent)
                .transfer(transfer)
                .build();

        cashOperationRepository.save(operation);
    }

    private void checkDailyLimit(Agency agency, BigDecimal amount) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);

        CashRegister cashRegister = cashRegisterRepository
                .findByAgencyId(agency.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "No cash register found for agency: " + agency.getId()));

        List<CashOperation> todayOps = cashOperationRepository
                .findAllByCashRegisterIdAndCreatedAtBetween(
                        cashRegister.getId(), startOfDay, endOfDay);

        BigDecimal todayTotal = todayOps.stream()
                .filter(op -> op.getType() == OperationType.ENVOI)
                .map(CashOperation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (todayTotal.add(amount).compareTo(agency.getDailyLimit()) > 0) {
            throw new IllegalStateException(
                    "Daily transfer limit exceeded for agency: " +
                            agency.getName() +
                            " | Limit: " + agency.getDailyLimit() +
                            " | Used today: " + todayTotal +
                            " | Requested: " + amount);
        }
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE — Client account resolution
    // ─────────────────────────────────────────────────────

    private User resolveClientAccount(String firstName,
                                      String lastName,
                                      String phone,
                                      String email) {
        // Check by email first
        if (email != null && !email.isBlank()) {
            Optional<User> existing = userRepository.findByEmail(email);
            if (existing.isPresent()) return existing.get();
        }

        // Auto-create client account
        CredentialsGenerator.GeneratedCredentials creds =
                credentialsGenerator.generate(firstName, lastName, email);

        User client = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(creds.getEmail())
                .password(passwordEncoder.encode(creds.getPlainPassword()))
                .phone(phone)
                .role(Role.ROLE_CLIENT)
                .active(true)
                .mustChangePassword(true)
                .twoFactorEnabled(false)
                .build();

        userRepository.save(client);

        // Send credentials
        notificationService.sendCredentials(
                client.getId(),
                creds.getEmail(),
                phone,
                creds.getPlainPassword()
        );

        log.info("Auto-created client account for: {} {}", firstName, lastName);
        return client;
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE — Finders
    // ─────────────────────────────────────────────────────

    private Transfer findTransfer(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Country findCountry(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country", id));
    }

    private Currency findCurrency(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Currency", id));
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE — Mapper
    // ─────────────────────────────────────────────────────
    private TransferResponse toResponse(Transfer transfer) {
        if (transfer == null) return null;

        String senderCountryName = transfer.getSenderCountry() != null
                ? transfer.getSenderCountry().getName()
                : null;

        String recipientCountryName = transfer.getRecipientCountry() != null
                ? transfer.getRecipientCountry().getName()
                : null;

        String corridorLabel = transfer.getCorridor() != null
                ? format("%s → %s (%s → %s)",
                transfer.getCorridor().getSourceCountry().getCode(),
                transfer.getCorridor().getDestinationCountry().getCode(),
                transfer.getCorridor().getSourceCurrency().getCode(),
                transfer.getCorridor().getDestinationCurrency().getCode())
                : null;

        return TransferResponse.builder()
                .id(transfer.getId())
                .withdrawalCode(transfer.getWithdrawalCode())
                .senderFullName(transfer.getSenderFirstName() + " " + transfer.getSenderLastName())
                .senderPhone(transfer.getSenderPhone())
                .senderCountry(senderCountryName)
                .recipientFullName(transfer.getRecipientFirstName() + " " + transfer.getRecipientLastName())
                .recipientPhone(transfer.getRecipientPhone())
                .recipientCountry(recipientCountryName)
                .sentAmount(transfer.getSentAmount())
                .sentCurrency(transfer.getSentCurrency() != null ? transfer.getSentCurrency().getCode() : null)
                .feeAmount(transfer.getFeeAmount())
                .receivedAmount(transfer.getReceivedAmount())
                .receivedCurrency(transfer.getReceivedCurrency() != null ? transfer.getReceivedCurrency().getCode() : null)
                .exchangeRate(transfer.getExchangeRate())
                .feeFixed(transfer.getFeeFixed())
                .feePercentage(transfer.getFeePercentage())
                .transferType(transfer.getTransferType())
                .status(transfer.getStatus())
                .requiresAdminApproval(transfer.isRequiresAdminApproval())
                .blockedReason(transfer.getBlockedReason())
                .cancellationReason(transfer.getCancellationReason())
                .notes(transfer.getNotes())
                .sendingAgencyId(transfer.getSendingAgency() != null ? transfer.getSendingAgency().getId() : null)
                .sendingAgencyName(transfer.getSendingAgency() != null ? transfer.getSendingAgency().getName() : null)
                .receivingAgencyId(transfer.getReceivingAgency() != null ? transfer.getReceivingAgency().getId() : null)
                .receivingAgencyName(transfer.getReceivingAgency() != null ? transfer.getReceivingAgency().getName() : null)
                .sendingAgentId(transfer.getSendingAgent() != null ? transfer.getSendingAgent().getId() : null)
                .sendingAgentName(transfer.getSendingAgent() != null
                        ? transfer.getSendingAgent().getFirstName() + " " + transfer.getSendingAgent().getLastName()
                        : null)
                .receivingAgentId(transfer.getReceivingAgent() != null ? transfer.getReceivingAgent().getId() : null)
                .receivingAgentName(transfer.getReceivingAgent() != null
                        ? transfer.getReceivingAgent().getFirstName() + " " + transfer.getReceivingAgent().getLastName()
                        : null)
                .clientId(transfer.getClient() != null ? transfer.getClient().getId() : null)
                .corridorId(transfer.getCorridor() != null ? transfer.getCorridor().getId() : null)
                .corridorLabel(corridorLabel)
                .createdAt(transfer.getCreatedAt())
                .paidAt(transfer.getPaidAt())
                .expiresAt(transfer.getExpiresAt())
                .build();
    }
}