package com.okanetransfer.service;

import com.okanetransfer.dto.request.CreateMobileMoneyRequest;
import com.okanetransfer.dto.response.MobileMoneyResponse;
import com.okanetransfer.entity.MobileMoney;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.enums.MobileMoneyStatus;
import com.okanetransfer.entity.enums.TransferType;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.MobileMoneyRepository;
import com.okanetransfer.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MobileMoneyService {

    private static final Logger log =
            LoggerFactory.getLogger(MobileMoneyService.class);

    @Autowired
    private MobileMoneyRepository mobileMoneyRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AuditService auditService;

    // ─────────────────────────────────────────────────────
    //  INITIATE — simulate sending to mobile wallet
    // ─────────────────────────────────────────────────────

    @Transactional
    public MobileMoneyResponse initiate(CreateMobileMoneyRequest request,
                                        Long agentId) {
        Transfer transfer = transferRepository.findById(request.getTransferId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transfer", request.getTransferId()));

        // Validate transfer type
        if (transfer.getTransferType() != TransferType.MOBILE_MONEY) {
            throw new IllegalArgumentException(
                    "Transfer is not of type MOBILE_MONEY");
        }

        // Check no mobile money record already exists
        if (mobileMoneyRepository.findByTransferId(transfer.getId()).isPresent()) {
            throw new IllegalStateException(
                    "Mobile money record already exists for this transfer");
        }

        // Simulate API call to mobile operator
        String operatorReference = simulateOperatorApiCall(
                request.getOperator().name(),
                request.getWalletPhone(),
                transfer.getReceivedAmount().toString()
        );

        MobileMoney mobileMoney = MobileMoney.builder()
                .transfer(transfer)
                .operator(request.getOperator())
                .walletPhone(request.getWalletPhone())
                .status(MobileMoneyStatus.SENT)
                .operatorReference(operatorReference)
                .sentAt(LocalDateTime.now())
                .build();

        MobileMoney saved = mobileMoneyRepository.save(mobileMoney);

        auditService.log(agentId, "MOBILE_MONEY_INITIATED",
                "MobileMoney", saved.getId(),
                "{\"operator\":\"" + request.getOperator() +
                        "\",\"reference\":\"" + operatorReference + "\"}");

        log.info("Mobile money initiated: operator={}, phone={}, ref={}",
                request.getOperator(), request.getWalletPhone(), operatorReference);

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────
    //  RECONCILE — admin confirms operator received funds
    // ─────────────────────────────────────────────────────

    @Transactional
    public MobileMoneyResponse reconcile(Long id, Long adminId) {
        MobileMoney mm = mobileMoneyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MobileMoney", id));

        if (mm.getStatus() != MobileMoneyStatus.SENT) {
            throw new IllegalStateException(
                    "Can only reconcile transfers in SENT status. " +
                            "Current: " + mm.getStatus());
        }

        mm.setStatus(MobileMoneyStatus.RECONCILED);
        mm.setReconciledAt(LocalDateTime.now());
        mobileMoneyRepository.save(mm);

        auditService.log(adminId, "MOBILE_MONEY_RECONCILED",
                "MobileMoney", id, null);

        return toResponse(mm);
    }

    // ─────────────────────────────────────────────────────
    //  QUERIES
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public MobileMoneyResponse getByTransfer(Long transferId) {
        MobileMoney mm = mobileMoneyRepository.findByTransferId(transferId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MobileMoney not found for transfer: " + transferId));
        return toResponse(mm);
    }

    @Transactional(readOnly = true)
    public List<MobileMoneyResponse> getPending() {
        return mobileMoneyRepository.findAllByStatus(MobileMoneyStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MobileMoneyResponse> getSent() {
        return mobileMoneyRepository.findAllByStatus(MobileMoneyStatus.SENT)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE — simulated API call
    // ─────────────────────────────────────────────────────

    /**
     * Simulates an API call to a mobile money operator.
     * In production: replace with real HTTP call to Orange/Wave/M-Pesa API.
     *
     * @return fake transaction reference
     */
    private String simulateOperatorApiCall(String operator,
                                           String phone,
                                           String amount) {
        // Simulate network delay
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        String fakeRef = operator.substring(0, 2).toUpperCase() +
                "-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        log.debug("[SIMULATED] {} API called → phone={}, amount={}, ref={}",
                operator, phone, amount, fakeRef);
        return fakeRef;
    }

    private MobileMoneyResponse toResponse(MobileMoney mm) {
        return MobileMoneyResponse.builder()
                .id(mm.getId())
                .transferId(mm.getTransfer().getId())
                .withdrawalCode(mm.getTransfer().getWithdrawalCode())
                .operator(mm.getOperator())
                .walletPhone(mm.getWalletPhone())
                .status(mm.getStatus())
                .operatorReference(mm.getOperatorReference())
                .sentAt(mm.getSentAt())
                .reconciledAt(mm.getReconciledAt())
                .build();
    }
}