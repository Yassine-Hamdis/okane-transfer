package com.okanetransfer.service;

import com.okanetransfer.dto.request.CloseCashRequest;
import com.okanetransfer.dto.request.DiscrepancyRequest;
import com.okanetransfer.dto.response.CashBalanceResponse;
import com.okanetransfer.dto.response.CashOperationResponse;
import com.okanetransfer.dto.response.CashRegisterResponse;
import com.okanetransfer.entity.*;
import com.okanetransfer.entity.enums.OperationType;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashService {

    @Autowired private CashRegisterRepository        cashRegisterRepository;
    @Autowired private CashRegisterBalanceRepository cashRegisterBalanceRepository;
    @Autowired private CashOperationRepository       cashOperationRepository;
    @Autowired private CurrencyRepository            currencyRepository;
    @Autowired private UserRepository                userRepository;
    @Autowired private AuditService                  auditService;

    // ─────────────────────────────────────────────────────
    //  GET REGISTER
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CashRegisterResponse getRegisterByAgency(Long agencyId) {
        CashRegister cashRegister = cashRegisterRepository
                .findByAgencyId(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cash register not found for agency: " + agencyId));

        List<CashBalanceResponse> balances = cashRegisterBalanceRepository
                .findAllByCashRegisterId(cashRegister.getId())
                .stream()
                .map(this::toBalanceResponse)
                .collect(Collectors.toList());

        return CashRegisterResponse.builder()
                .id(cashRegister.getId())
                .agencyId(cashRegister.getAgency().getId())
                .agencyName(cashRegister.getAgency().getName())
                .balances(balances)
                .lastClosedAt(cashRegister.getLastClosedAt())
                .build();
    }

    // ─────────────────────────────────────────────────────
    //  GET TODAY OPERATIONS
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CashOperationResponse> getTodayOperations(Long agencyId) {
        CashRegister cashRegister = cashRegisterRepository
                .findByAgencyId(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cash register not found for agency: " + agencyId));

        LocalDateTime startOfDay = LocalDateTime.now()
                .toLocalDate().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);

        return cashOperationRepository
                .findAllByCashRegisterIdAndCreatedAtBetween(
                        cashRegister.getId(), startOfDay, endOfDay)
                .stream()
                .map(this::toOperationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────
    //  GET OPERATIONS HISTORY
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CashOperationResponse> getAllOperations(Long agencyId) {
        CashRegister cashRegister = cashRegisterRepository
                .findByAgencyId(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cash register not found for agency: " + agencyId));

        return cashOperationRepository
                .findAllByCashRegisterIdOrderByCreatedAtDesc(cashRegister.getId())
                .stream()
                .map(this::toOperationResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────
    //  CLOSE CASH REGISTER
    // ─────────────────────────────────────────────────────

    @Transactional
    public CashRegisterResponse closeCashRegister(Long agencyId,
                                                  Long agentId,
                                                  CloseCashRequest request) {
        CashRegister cashRegister = cashRegisterRepository
                .findByAgencyId(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cash register not found for agency: " + agencyId));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", agentId));

        // Record CLOTURE_CAISSE operation for each currency balance
        List<CashRegisterBalance> balances =
                cashRegisterBalanceRepository
                        .findAllByCashRegisterId(cashRegister.getId());

        balances.forEach(balance -> {
            CashOperation closeOp = CashOperation.builder()
                    .cashRegister(cashRegister)
                    .type(OperationType.CLOTURE_CAISSE)
                    .amount(balance.getCurrentBalance())
                    .balanceAfter(balance.getCurrentBalance())
                    .currency(balance.getCurrency())
                    .agent(agent)
                    .transfer(null)
                    .note(request.getNote() != null
                            ? request.getNote()
                            : "End of day cash register closure")
                    .build();
            cashOperationRepository.save(closeOp);
        });

        cashRegister.setLastClosedAt(LocalDateTime.now());
        cashRegisterRepository.save(cashRegister);

        auditService.log(agentId, "CASH_REGISTER_CLOSED",
                "CashRegister", cashRegister.getId(), null);

        return getRegisterByAgency(agencyId);
    }

    // ─────────────────────────────────────────────────────
    //  REPORT DISCREPANCY
    // ─────────────────────────────────────────────────────

    @Transactional
    public void reportDiscrepancy(Long agencyId,
                                  Long agentId,
                                  DiscrepancyRequest request) {
        CashRegister cashRegister = cashRegisterRepository
                .findByAgencyId(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cash register not found for agency: " + agencyId));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", agentId));

        Currency currency = currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Currency", request.getCurrencyId()));

        CashRegisterBalance balance = cashRegisterBalanceRepository
                .findByCashRegisterIdAndCurrencyId(
                        cashRegister.getId(), currency.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No balance found for this currency in this register"));

        CashOperation discrepancy = CashOperation.builder()
                .cashRegister(cashRegister)
                .type(OperationType.CLOTURE_CAISSE)
                .amount(request.getAmount())
                .balanceAfter(balance.getCurrentBalance())
                .currency(currency)
                .agent(agent)
                .transfer(null)
                .note("DISCREPANCY: " + request.getNote())
                .build();

        cashOperationRepository.save(discrepancy);

        auditService.log(agentId, "CASH_DISCREPANCY_REPORTED",
                "CashRegister", cashRegister.getId(),
                "{\"amount\":" + request.getAmount() +
                        ",\"currency\":\"" + currency.getCode() + "\"}");
    }

    // ─────────────────────────────────────────────────────
    //  MAPPERS
    // ─────────────────────────────────────────────────────

    private CashBalanceResponse toBalanceResponse(CashRegisterBalance b) {
        return CashBalanceResponse.builder()
                .currencyId(b.getCurrency().getId())
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
                .currencyCode(op.getCurrency().getCode())
                .balanceAfter(op.getBalanceAfter())
                .agentId(op.getAgent().getId())
                .agentName(op.getAgent().getFirstName() + " "
                        + op.getAgent().getLastName())
                .transferId(op.getTransfer() != null
                        ? op.getTransfer().getId() : null)
                .withdrawalCode(op.getTransfer() != null
                        ? op.getTransfer().getWithdrawalCode() : null)
                .note(op.getNote())
                .createdAt(op.getCreatedAt())
                .build();
    }
}
