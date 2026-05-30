package com.okanetransfer.service;

import com.okanetransfer.dto.response.CashBalanceResponse;
import com.okanetransfer.dto.response.CashOperationResponse;
import com.okanetransfer.dto.response.CashRegisterResponse;
import com.okanetransfer.dto.request.CloseCashRequest;
import com.okanetransfer.dto.request.DiscrepancyRequest;
import com.okanetransfer.entity.*;
import com.okanetransfer.entity.enums.OperationType;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.*;
import com.okanetransfer.service.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CashService {

    private final CashRegisterRepository        registerRepository;
    private final CashRegisterBalanceRepository balanceRepository;
    private final CashOperationRepository       operationRepository;
    private final AgencyRepository              agencyRepository;
    private final CurrencyRepository            currencyRepository;
    private final AuditService                  auditService;

    public CashService(CashRegisterRepository registerRepository,
                       CashRegisterBalanceRepository balanceRepository,
                       CashOperationRepository operationRepository,
                       AgencyRepository agencyRepository,
                       CurrencyRepository currencyRepository,
                       AuditService auditService) {
        this.registerRepository  = registerRepository;
        this.balanceRepository   = balanceRepository;
        this.operationRepository = operationRepository;
        this.agencyRepository    = agencyRepository;
        this.currencyRepository  = currencyRepository;
        this.auditService        = auditService;
    }

    // ── Called internally by TransferService ───────────────────────────────────

    /**
     * Record a cash movement and update the running balance.
     * Called automatically by TransferService on create / payout / cancel.
     */
    public void recordOperation(Agency agency,
                                User agent,
                                OperationType type,
                                BigDecimal amount,
                                Currency currency,
                                Transfer transfer) {

        CashRegister register = getOrCreateRegister(agency);

        // Get or create balance row for this currency
        CashRegisterBalance balance = balanceRepository
                .findByCashRegisterIdAndCurrencyId(register.getId(), currency.getId())
                .orElseGet(() -> CashRegisterBalance.builder()
                        .cashRegister(register)
                        .currency(currency)
                        .currentBalance(BigDecimal.ZERO)
                        .build());

        // ENVOI = money received at counter → balance increases
        // RETRAIT / ANNULATION = money paid out → balance decreases
        BigDecimal newBalance = (type == OperationType.ENVOI)
                ? balance.getCurrentBalance().add(amount)
                : balance.getCurrentBalance().subtract(amount);

        balance.setCurrentBalance(newBalance);
        balanceRepository.save(balance);

        // Record the operation line
        CashOperation op = CashOperation.builder()
                .cashRegister(register)
                .type(type)
                .amount(amount)
                .currency(currency)
                .balanceAfter(newBalance)
                .agent(agent)
                .transfer(transfer)
                .build();

        operationRepository.save(op);
    }

    // ── Agent endpoints ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CashRegisterResponse getRegisterByAgency(Long agencyId) {
        Agency agency = findAgency(agencyId);
        CashRegister register = getOrCreateRegister(agency);

        List<CashBalanceResponse> balances = balanceRepository
                .findAllByCashRegisterId(register.getId())
                .stream()
                .map(this::toBalanceResponse)
                .collect(Collectors.toList());

        return CashRegisterResponse.builder()
                .id(register.getId())
                .agencyId(agencyId)
                .balances(balances)
                .lastClosedAt(register.getLastClosedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CashBalanceResponse> getBalances(Long agencyId) {
        Agency agency = findAgency(agencyId);
        CashRegister register = getOrCreateRegister(agency);
        return balanceRepository.findAllByCashRegisterId(register.getId())
                .stream().map(this::toBalanceResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CashOperationResponse> getTodayOperations(Long agencyId) {
        Agency agency = findAgency(agencyId);
        CashRegister register = getOrCreateRegister(agency);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        return operationRepository
                .findAllByCashRegisterIdAndCreatedAtBetween(register.getId(), startOfDay, now)
                .stream()
                .map(this::toOperationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Close the cash register — records a CLOTURE_CAISSE operation
     * and stamps lastClosedAt.
     */
    public void closeCashRegister(Long agencyId, User agent, CloseCashRequest request) {
        Agency agency = findAgency(agencyId);
        CashRegister register = getOrCreateRegister(agency);

        CashOperation closeOp = CashOperation.builder()
                .cashRegister(register)
                .type(OperationType.CLOTURE_CAISSE)
                .amount(BigDecimal.ZERO)
                .balanceAfter(BigDecimal.ZERO)
                .agent(agent)
                .note(request.getNote())
                .build();

        operationRepository.save(closeOp);

        register.setLastClosedAt(LocalDateTime.now());
        registerRepository.save(register);

        auditService.log(
                agent.getId(),
                "CASH_REGISTER_CLOSED",
                "CashRegister",
                register.getId(),
                "agencyId=" + agencyId + ", note=" + request.getNote()
        );
    }

    /**
     * Report a cash discrepancy found during end-of-day reconciliation.
     */
    public void reportDiscrepancy(Long agencyId, User agent, DiscrepancyRequest request) {
        Agency agency = findAgency(agencyId);
        CashRegister register = getOrCreateRegister(agency);

        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getCurrencyId()));

        CashRegisterBalance balance = balanceRepository
                .findByCashRegisterIdAndCurrencyId(register.getId(), currency.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No balance found for currency: " + currency.getCode()));

        CashOperation op = CashOperation.builder()
                .cashRegister(register)
                .type(OperationType.CLOTURE_CAISSE)
                .amount(request.getAmount())
                .currency(currency)
                .balanceAfter(balance.getCurrentBalance())
                .agent(agent)
                .note("DISCREPANCY: " + request.getNote())
                .build();

        operationRepository.save(op);

        auditService.log(
                agent.getId(),
                "CASH_DISCREPANCY_REPORTED",
                "CashRegister",
                register.getId(),
                "agencyId=" + agencyId
                        + ", amount=" + request.getAmount()
                        + ", currency=" + currency.getCode()
                        + ", note=" + request.getNote()
        );
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private CashRegister getOrCreateRegister(Agency agency) {
        return registerRepository.findByAgencyId(agency.getId())
                .orElseGet(() -> {
                    CashRegister r = CashRegister.builder()
                            .agency(agency)
                            .build();
                    return registerRepository.save(r);
                });
    }

    private Agency findAgency(Long agencyId) {
        return agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", agencyId));
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private CashBalanceResponse toBalanceResponse(CashRegisterBalance b) {
        return CashBalanceResponse.builder()
                .currencyCode(b.getCurrency().getCode())
                .currencySymbol(b.getCurrency().getSymbol())
                .currentBalance(b.getCurrentBalance())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    private CashOperationResponse toOperationResponse(CashOperation op) {
        return CashOperationResponse.builder()
                .id(op.getId())
                .type(op.getType())
                .amount(op.getAmount())
                .currencyCode(op.getCurrency() != null ? op.getCurrency().getCode() : null)
                .balanceAfter(op.getBalanceAfter())
                .agentName(op.getAgent().getFirstName() + " " + op.getAgent().getLastName())
                .transferId(op.getTransfer() != null ? op.getTransfer().getId() : null)
                .note(op.getNote())
                .createdAt(op.getCreatedAt())
                .build();
    }
}
