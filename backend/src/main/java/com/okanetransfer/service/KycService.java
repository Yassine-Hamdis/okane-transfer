package com.okanetransfer.service;

import com.okanetransfer.dto.request.KycReviewRequest;
import com.okanetransfer.dto.response.KycRecordResponse;
import com.okanetransfer.entity.KycRecord;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.KycStatus;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.KycRecordRepository;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KycService {

    private static final Logger log = LoggerFactory.getLogger(KycService.class);

    // Amount threshold above which suspicion is auto-declared
    private static final double SUSPICION_THRESHOLD = 10_000.0;

    // Risk score thresholds
    private static final int SCORE_FLAG_THRESHOLD  = 60;
    private static final int SCORE_BLOCK_THRESHOLD = 85;

    // Mock OFAC watchlist — in reality this would be a real list or API
    private static final List<String> MOCK_OFAC_LIST = Arrays.asList(
            "JOHN DOE",
            "JANE SMITH",
            "TEST BLOCKED",
            "OFAC SUSPECT"
    );

    @Autowired
    private KycRecordRepository kycRecordRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    // ─────────────────────────────────────────────────────
    //  AUTO CHECK — called by TransferService on creation
    // ─────────────────────────────────────────────────────

    /**
     * Automatically runs KYC checks when a transfer is created.
     * Called by TransferService — never by controller directly.
     *
     * Steps:
     *  1. Calculate risk score
     *  2. Check recipient name against mock OFAC list
     *  3. Check if amount exceeds suspicion threshold
     *  4. Set final status: PASSED / FLAGGED / BLOCKED
     *  5. If BLOCKED → mark transfer as requiring admin approval
     *  6. Save and return
     */
    @Transactional
    public KycRecord autoCheck(Transfer transfer) {
        String recipientFullName = (
                transfer.getRecipientFirstName() + " " +
                        transfer.getRecipientLastName()).toUpperCase();

        // ── 1. Watchlist check ────────────────────────
        boolean watchlistHit = MOCK_OFAC_LIST.stream()
                .anyMatch(name -> recipientFullName.contains(name));

        // ── 2. Risk score calculation ─────────────────
        int riskScore = calculateRiskScore(
                transfer.getSentAmount().doubleValue(),
                watchlistHit
        );

        // ── 3. Suspicion threshold ────────────────────
        boolean suspicionDeclared =
                transfer.getSentAmount().doubleValue() >= SUSPICION_THRESHOLD;

        // ── 4. Determine status ───────────────────────
        KycStatus status;
        if (watchlistHit || riskScore >= SCORE_BLOCK_THRESHOLD) {
            status = KycStatus.BLOCKED;
        } else if (suspicionDeclared || riskScore >= SCORE_FLAG_THRESHOLD) {
            status = KycStatus.FLAGGED;
        } else {
            status = KycStatus.PASSED;
        }

        // ── 5. Flag transfer if blocked ───────────────
        if (status == KycStatus.BLOCKED) {
            transfer.setRequiresAdminApproval(true);
            transfer.setBlockedReason(
                    watchlistHit
                            ? "Recipient name matches OFAC watchlist"
                            : "Risk score exceeded block threshold: " + riskScore);
            transferRepository.save(transfer);
            log.warn("Transfer {} BLOCKED by KYC — recipient={}, score={}",
                    transfer.getWithdrawalCode(), recipientFullName, riskScore);
        }

        // ── 6. Save KYC record ────────────────────────
        KycRecord record = KycRecord.builder()
                .transfer(transfer)
                .status(status)
                .watchlistHit(watchlistHit)
                .suspicionDeclared(suspicionDeclared)
                .riskScore(riskScore)
                .notes(buildAutoNotes(watchlistHit, suspicionDeclared, riskScore))
                .checkedBy(null)
                .checkedAt(LocalDateTime.now())
                .build();

        KycRecord saved = kycRecordRepository.save(record);

        auditService.log(null, "KYC_AUTO_CHECK", "Transfer",
                transfer.getId(),
                "{\"status\":\"" + status + "\",\"score\":" + riskScore + "}");

        return saved;
    }

    // ─────────────────────────────────────────────────────
    //  MANUAL REVIEW — called by Admin
    // ─────────────────────────────────────────────────────

    @Transactional
    public KycRecordResponse manualReview(Long kycId,
                                          KycReviewRequest request,
                                          Long adminId) {
        KycRecord record = kycRecordRepository.findById(kycId)
                .orElseThrow(() -> new ResourceNotFoundException("KycRecord", kycId));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId));

        KycStatus oldStatus = record.getStatus();
        record.setStatus(request.getStatus());
        record.setNotes(request.getNotes());
        record.setCheckedBy(admin);
        record.setCheckedAt(LocalDateTime.now());

        // If admin approves a blocked transfer → unblock it
        if (request.getStatus() == KycStatus.PASSED) {
            Transfer transfer = record.getTransfer();
            transfer.setRequiresAdminApproval(false);
            transfer.setBlockedReason(null);
            transferRepository.save(transfer);
        }

        kycRecordRepository.save(record);

        auditService.log(adminId, "KYC_MANUAL_REVIEW", "KycRecord", kycId,
                "{\"oldStatus\":\"" + oldStatus +
                        "\",\"newStatus\":\"" + request.getStatus() + "\"}");

        return toResponse(record);
    }

    // ─────────────────────────────────────────────────────
    //  QUERIES
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public KycRecordResponse getByTransfer(Long transferId) {
        KycRecord record = kycRecordRepository.findByTransferId(transferId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "KycRecord not found for transfer: " + transferId));
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<KycRecordResponse> getFlagged() {
        return kycRecordRepository.findAllByStatus(KycStatus.FLAGGED)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KycRecordResponse> getBlocked() {
        return kycRecordRepository.findAllByStatus(KycStatus.BLOCKED)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KycRecordResponse> getWatchlistHits() {
        return kycRecordRepository.findAllByWatchlistHitTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<KycRecordResponse> getSuspicionDeclared() {
        return kycRecordRepository.findAllBySuspicionDeclaredTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ─────────────────────────────────────────────────────

    /**
     * Risk score 0–100 based on amount and watchlist hit.
     *
     * Scoring logic:
     *  - Base score from amount ranges
     *  - Watchlist hit adds 50 points (almost certainly blocks)
     *  - Capped at 100
     */
    private int calculateRiskScore(double amount, boolean watchlistHit) {
        int score = 0;

        if (amount >= 50_000)     score = 80;
        else if (amount >= 20_000) score = 65;
        else if (amount >= 10_000) score = 50;
        else if (amount >= 5_000)  score = 35;
        else if (amount >= 1_000)  score = 15;
        else                       score = 5;

        if (watchlistHit) score += 50;

        return Math.min(score, 100);
    }

    private String buildAutoNotes(boolean watchlistHit,
                                  boolean suspicionDeclared,
                                  int riskScore) {
        StringBuilder sb = new StringBuilder("Auto-check: ");
        sb.append("riskScore=").append(riskScore);
        if (watchlistHit)       sb.append(", OFAC_HIT");
        if (suspicionDeclared)  sb.append(", SUSPICION_THRESHOLD_EXCEEDED");
        return sb.toString();
    }

    private KycRecordResponse toResponse(KycRecord r) {
        return KycRecordResponse.builder()
                .id(r.getId())
                .transferId(r.getTransfer().getId())
                .withdrawalCode(r.getTransfer().getWithdrawalCode())
                .status(r.getStatus())
                .watchlistHit(r.isWatchlistHit())
                .suspicionDeclared(r.isSuspicionDeclared())
                .riskScore(r.getRiskScore())
                .notes(r.getNotes())
                .checkedById(r.getCheckedBy() != null
                        ? r.getCheckedBy().getId() : null)
                .checkedByEmail(r.getCheckedBy() != null
                        ? r.getCheckedBy().getEmail() : null)
                .checkedAt(r.getCheckedAt())
                .build();
    }
}