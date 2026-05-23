package com.okanetransfer.service;

import com.okanetransfer.dto.request.CreateTransferRequest;
import com.okanetransfer.dto.request.PayoutRequest;
import com.okanetransfer.dto.response.TransferResponse;
import com.okanetransfer.dto.response.TransferSummaryResponse;
import com.okanetransfer.dto.response.TransferTrackResponse;
import com.okanetransfer.entity.*;
import com.okanetransfer.entity.enums.OperationType;
import com.okanetransfer.entity.enums.TransferStatus;
import com.okanetransfer.entity.enums.TransferType;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.*;
import com.okanetransfer.util.AesEncryptionUtil;
import com.okanetransfer.util.WithdrawalCodeGenerator;
import com.okanetransfer.service.AuditService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransferService {

    // Transfers over this threshold need admin approval
    private static final BigDecimal APPROVAL_THRESHOLD = new BigDecimal("10000");

    private final TransferRepository   transferRepository;
    private final UserRepository       userRepository;
    private final AgencyRepository     agencyRepository;
    private final CorridorRepository   corridorRepository;
    private final CountryRepository    countryRepository;
    private final CurrencyRepository   currencyRepository;
    private final AesEncryptionUtil    aesUtil;
    private final WithdrawalCodeGenerator codeGenerator;
    private final CashService          cashService;

    private final AuditService       auditService;

    // NOTE: FeeGridService injected via interface once Person 2 delivers it.
    private final FeeGridServicePort feeGridService;

    public TransferService(TransferRepository transferRepository,
                           UserRepository userRepository,
                           AgencyRepository agencyRepository,
                           CorridorRepository corridorRepository,
                           CountryRepository countryRepository,
                           CurrencyRepository currencyRepository,
                           AesEncryptionUtil aesUtil,
                           WithdrawalCodeGenerator codeGenerator,
                           CashService cashService,
                           AuditService auditService,
                           FeeGridServicePort feeGridService) {
        this.transferRepository = transferRepository;
        this.userRepository     = userRepository;
        this.agencyRepository   = agencyRepository;
        this.corridorRepository = corridorRepository;
        this.countryRepository  = countryRepository;
        this.currencyRepository = currencyRepository;
        this.aesUtil            = aesUtil;
        this.codeGenerator      = codeGenerator;
        this.cashService        = cashService;
        this.auditService       = auditService;
        this.feeGridService     = feeGridService;
    }

    // ── Create Transfer (10-step CDC flow) ─────────────────────────────────────

    public TransferResponse createTransfer(CreateTransferRequest req,
                                           Long agentId,
                                           Long agencyId) {

        // Step 1 — Resolve entities
        User   agent    = findUser(agentId);
        Agency agency   = findAgency(agencyId);
        Corridor corridor = corridorRepository.findById(req.getCorridorId())
                .orElseThrow(() -> new ResourceNotFoundException("Corridor", req.getCorridorId()));
        Country senderCountry    = countryRepository.findById(req.getSenderCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", req.getSenderCountryId()));
        Country recipientCountry = countryRepository.findById(req.getRecipientCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", req.getRecipientCountryId()));
        Currency sentCurrency    = currencyRepository.findById(req.getSentCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", req.getSentCurrencyId()));

        // Step 2 — Validate corridor is active
        if (!corridor.isActive()) {
            throw new IllegalArgumentException("Corridor is not active: " + corridor.getId());
        }

        // Step 3 — Fee simulation (from Person 2)
        FeeSimulationResult sim = feeGridService.simulateFee(
                corridor.getId(), req.getSentAmount(),
                sentCurrency.getId(), req.getTransferType());

        Currency receivedCurrency = currencyRepository.findById(sim.receivedCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", sim.receivedCurrencyId()));

        // Step 4 — Check agency daily limit
        BigDecimal todayVolume = calculateTodayVolume(agencyId);
        if (todayVolume.add(req.getSentAmount()).compareTo(agency.getDailyLimit()) > 0) {
            throw new IllegalStateException("Agency daily transfer limit exceeded");
        }

        // Step 5 — Generate unique withdrawal code
        WithdrawalCodeGenerator.WithdrawalCode bundle = codeGenerator.generate();

        // Step 6 — Build entity with encrypted sender ID
        Transfer transfer = Transfer.builder()
                .withdrawalCode(bundle.getCode())
                // Sender
                .senderFirstName(req.getSenderFirstName())
                .senderLastName(req.getSenderLastName())
                .senderPhone(req.getSenderPhone())
                .senderIdEncrypted(aesUtil.encrypt(req.getSenderIdNumber()))
                .senderCountry(senderCountry)
                // Recipient
                .recipientFirstName(req.getRecipientFirstName())
                .recipientLastName(req.getRecipientLastName())
                .recipientPhone(req.getRecipientPhone())
                .recipientCountry(recipientCountry)
                // Financials
                .sentAmount(req.getSentAmount())
                .sentCurrency(sentCurrency)
                .feeAmount(sim.feeAmount())
                .feeFixed(sim.feeFixed())
                .feePercentage(sim.feePercentage())
                .receivedAmount(sim.receivedAmount())
                .receivedCurrency(receivedCurrency)
                .exchangeRate(sim.exchangeRate())
                // Metadata
                .transferType(req.getTransferType())
                .corridor(corridor)
                .sendingAgency(agency)
                .sendingAgent(agent)
                // Step 7 — Admin approval check
                .requiresAdminApproval(req.getSentAmount().compareTo(APPROVAL_THRESHOLD) > 0)
                .build();

        // @PrePersist sets createdAt and expiresAt (30 days) automatically
        Transfer saved = transferRepository.save(transfer);

        // Step 8 — Create ENVOI cash operation
        cashService.recordOperation(
                agency, agent,
                OperationType.ENVOI,
                req.getSentAmount(),
                sentCurrency,
                saved
        );

        // Step 9 — KYC check triggered by Person 4's KycService (called from controller)

        // Step 10 — Audit log
        auditService.log(
                agentId,
                "TRANSFER_CREATED",
                "Transfer",
                saved.getId(),
                "code=" + saved.getWithdrawalCode()
                        + ", amount=" + saved.getSentAmount()
                        + ", corridor=" + saved.getCorridor().getId()
        );

        return toFullResponse(saved);
    }

    // ── Payout (10-step CDC flow) ──────────────────────────────────────────────

    public TransferResponse processPayment(PayoutRequest req,
                                           Long agentId,
                                           Long agencyId) {

        User   receivingAgent  = findUser(agentId);
        Agency receivingAgency = findAgency(agencyId);

        // Step 1 — Find by withdrawal code
        Transfer transfer = transferRepository.findByWithdrawalCode(req.getWithdrawalCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No transfer found for code: " + req.getWithdrawalCode()));

        // Step 2 — Status check
        if (transfer.getStatus() != TransferStatus.EN_ATTENTE) {
            throw new IllegalStateException(
                    "Transfer cannot be paid out — current status: " + transfer.getStatus());
        }

        // Step 3 — Expiry check
        if (LocalDateTime.now().isAfter(transfer.getExpiresAt())) {
            transfer.setStatus(TransferStatus.EXPIRE);
            transferRepository.save(transfer);
            throw new IllegalStateException("Transfer has expired");
        }

        // Step 4 — Encrypt and save recipient ID
        transfer.setRecipientIdEncrypted(aesUtil.encrypt(req.getRecipientIdNumber()));

        // Step 5 — Mark as paid
        transfer.setStatus(TransferStatus.PAYE);
        transfer.setReceivingAgent(receivingAgent);
        transfer.setReceivingAgency(receivingAgency);
        transfer.setPaidAt(LocalDateTime.now());

        Transfer saved = transferRepository.save(transfer);

        // Step 6 — Create RETRAIT cash operation
        cashService.recordOperation(
                receivingAgency, receivingAgent,
                OperationType.RETRAIT,
                transfer.getReceivedAmount(),
                transfer.getReceivedCurrency(),
                saved
        );

        // Step 7 — Notification triggered by Person 4's NotificationService (from controller)

        // Step 8 — Audit log
        auditService.log(
                agentId,
                "TRANSFER_PAID",
                "Transfer",
                saved.getId(),
                "code=" + saved.getWithdrawalCode()
                        + ", paidAt=" + saved.getPaidAt()
        );

        return toFullResponse(saved);
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    public void cancelTransfer(Long id, String reason, Long agentId) {
        Transfer transfer = findTransfer(id);
        User agent = findUser(agentId);

        if (transfer.getStatus() != TransferStatus.EN_ATTENTE) {
            throw new IllegalStateException("Only EN_ATTENTE transfers can be cancelled");
        }

        transfer.setStatus(TransferStatus.ANNULE);
        transfer.setCancellationReason(reason);
        transferRepository.save(transfer);

        // Reverse the ENVOI cash operation (refund to agency)
        cashService.recordOperation(
                transfer.getSendingAgency(), agent,
                OperationType.ANNULATION,
                transfer.getSentAmount(),
                transfer.getSentCurrency(),
                transfer
        );

        // Audit log
        auditService.log(
                agentId,
                "TRANSFER_CANCELLED",
                "Transfer",
                id,
                "reason=" + reason
        );
    }

    // ── Admin: approve ────────────────────────────────────────────────────────

    public TransferResponse approveTransfer(Long id, Long adminId) {
        Transfer transfer = findTransfer(id);
        transfer.setRequiresAdminApproval(false);
        Transfer saved = transferRepository.save(transfer);

        auditService.log(
                adminId,
                "TRANSFER_APPROVED",
                "Transfer",
                id,
                "approvedBy=" + adminId
        );

        return toFullResponse(saved);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public TransferResponse getById(Long id) {
        return toFullResponse(findTransfer(id));
    }

    @Transactional(readOnly = true)
    public TransferResponse getByWithdrawalCode(String code) {
        return toFullResponse(
                transferRepository.findByWithdrawalCode(code)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Transfer not found for code: " + code)));
    }

    @Transactional(readOnly = true)
    public List<TransferSummaryResponse> getByAgent(Long agentId) {
        return transferRepository.findAllBySendingAgentId(agentId)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferSummaryResponse> getByClient(Long clientId) {
        return transferRepository.findAllByClientId(clientId)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferSummaryResponse> getByAgency(Long agencyId) {
        return transferRepository.findAllBySendingAgencyId(agencyId)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferSummaryResponse> getAll() {
        return transferRepository.findAll()
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferSummaryResponse> searchByRecipientPhone(String phone) {
        return transferRepository.findAllByRecipientPhone(phone)
                .stream().map(this::toSummary).collect(Collectors.toList());
    }

    /**
     * Public tracking endpoint for clients - returns limited info by withdrawal code.
     * Used by GET /api/client/transfers/track/{code}
     */
    @Transactional(readOnly = true)
    public TransferTrackResponse trackByCode(String code) {
        Transfer t = transferRepository.findByWithdrawalCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transfer not found for code: " + code));
        return toTrackResponse(t);
    }

    // ── Scheduled expiry job ──────────────────────────────────────────────────

    @Scheduled(cron = "0 0 * * * *")   // every hour
    public void expireOldTransfers() {
        List<Transfer> expired = transferRepository
                .findAllByStatusAndExpiresAtBefore(TransferStatus.EN_ATTENTE, LocalDateTime.now());
        expired.forEach(t -> t.setStatus(TransferStatus.EXPIRE));
        transferRepository.saveAll(expired);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BigDecimal calculateTodayVolume(Long agencyId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return transferRepository.findAllBySendingAgencyId(agencyId).stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(startOfDay))
                .filter(t -> t.getStatus() != TransferStatus.ANNULE)
                .map(Transfer::getSentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Transfer findTransfer(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Agency findAgency(Long id) {
        return agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", id));
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private TransferResponse toFullResponse(Transfer t) {
        return TransferResponse.builder()
                .id(t.getId())
                .withdrawalCode(t.getWithdrawalCode())
                .status(t.getStatus())
                .senderFullName(t.getSenderFirstName() + " " + t.getSenderLastName())
                .recipientFullName(t.getRecipientFirstName() + " " + t.getRecipientLastName())
                .sentAmount(t.getSentAmount())
                .sentCurrencyCode(t.getSentCurrency() != null ? t.getSentCurrency().getCode() : null)
                .feeAmount(t.getFeeAmount())
                .receivedAmount(t.getReceivedAmount())
                .receivedCurrencyCode(t.getReceivedCurrency() != null ? t.getReceivedCurrency().getCode() : null)
                .exchangeRate(t.getExchangeRate())
                .transferType(t.getTransferType())
                .sendingAgencyName(t.getSendingAgency() != null ? t.getSendingAgency().getName() : null)
                .requiresAdminApproval(t.isRequiresAdminApproval())
                .createdAt(t.getCreatedAt())
                .expiresAt(t.getExpiresAt())
                .paidAt(t.getPaidAt())
                .build();
    }

    private TransferSummaryResponse toSummary(Transfer t) {
        return TransferSummaryResponse.builder()
                .id(t.getId())
                .withdrawalCode(t.getWithdrawalCode())
                .status(t.getStatus())
                .transferType(t.getTransferType())
                .recipientFullName(t.getRecipientFirstName() + " " + t.getRecipientLastName())
                .sentAmount(t.getSentAmount())
                .sentCurrency(t.getSentCurrency() != null ? t.getSentCurrency().getCode() : null)
                .receivedAmount(t.getReceivedAmount())
                .receivedCurrency(t.getReceivedCurrency() != null ? t.getReceivedCurrency().getCode() : null)
                .createdAt(t.getCreatedAt())
                .build();
    }

    private TransferTrackResponse toTrackResponse(Transfer t) {
        return TransferTrackResponse.builder()
                .withdrawalCode(t.getWithdrawalCode())
                .status(t.getStatus())
                .transferType(t.getTransferType())
                .recipientFullName(t.getRecipientFirstName() + " " + t.getRecipientLastName())
                .receivedAmount(t.getReceivedAmount())
                .receivedCurrency(t.getReceivedCurrency() != null ? t.getReceivedCurrency().getCode() : null)
                .sendingAgency(t.getSendingAgency() != null ? t.getSendingAgency().getName() : null)
                .createdAt(t.getCreatedAt())
                .expiresAt(t.getExpiresAt())
                .paidAt(t.getPaidAt())
                .build();
    }

    // ── Port interface (anti-corruption — Person 2 implements this) ───────────

    public interface FeeGridServicePort {
        void assertCorridorActive(Long corridorId);
        FeeSimulationResult simulateFee(Long corridorId, BigDecimal amount,
                                        Long currencyId, TransferType type);
    }

    public record FeeSimulationResult(
            BigDecimal feeAmount,
            BigDecimal feeFixed,
            BigDecimal feePercentage,
            BigDecimal receivedAmount,
            Long       receivedCurrencyId,
            BigDecimal exchangeRate
    ) {}
}
