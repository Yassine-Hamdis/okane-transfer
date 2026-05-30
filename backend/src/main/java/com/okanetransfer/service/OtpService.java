package com.okanetransfer.service;

import com.okanetransfer.entity.OtpCode;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.OtpType;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.OtpCodeRepository;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.AuditService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class OtpService {

    private static final int OTP_LENGTH         = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS       = 5;

    private final OtpCodeRepository    otpRepository;
    private final UserRepository       userRepository;
    private final TransferRepository   transferRepository;
    private final PasswordEncoder encoder;
    private final SecureRandom         random = new SecureRandom();
    private final AuditService         auditService;

    public OtpService(OtpCodeRepository otpRepository,
                      UserRepository userRepository,
                      TransferRepository transferRepository,
                      PasswordEncoder encoder,
                      AuditService auditService) {
        this.otpRepository      = otpRepository;
        this.userRepository     = userRepository;
        this.transferRepository = transferRepository;
        this.encoder            = encoder;
        this.auditService       = auditService;
    }

    // ── 2FA OTP for a user ─────────────────────────────────────────────────────

    /**
     * Generate a 6-digit 2FA OTP for a user.
     * Any previous unused OTP of the same type is invalidated first.
     *
     * @return plain-text code — send via SMS/email, never store it plain
     */
    public String generateForUser(Long userId, OtpType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Invalidate all previous unused OTPs for this user + type
        otpRepository.findAllByUserId(userId).stream()
                .filter(o -> o.getType() == type && !o.isUsed())
                .forEach(o -> {
                    o.setUsed(true);
                    otpRepository.save(o);
                });

        String plain = generateNumericCode();

        OtpCode otp = OtpCode.builder()
                .user(user)
                .type(type)
                .codeHash(encoder.encode(plain))
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        otpRepository.save(otp);
        return plain;
    }

    // ── Withdrawal OTP for a transfer ──────────────────────────────────────────

    /**
     * Generate a withdrawal OTP linked to a specific transfer.
     * Invalidates any previous withdrawal OTPs for the same transfer.
     *
     * @return plain-text code
     */
    public String generateForTransfer(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", transferId));

        // Invalidate previous withdrawal OTPs for this transfer
        otpRepository.findAllByTransferId(transferId).stream()
                .filter(o -> !o.isUsed())
                .forEach(o -> {
                    o.setUsed(true);
                    otpRepository.save(o);
                });

        String plain = generateNumericCode();

        OtpCode otp = OtpCode.builder()
                .transfer(transfer)
                .type(OtpType.WITHDRAWAL_VERIFY)
                .codeHash(encoder.encode(plain))
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        otpRepository.save(otp);
        return plain;
    }

    // ── Verification ───────────────────────────────────────────────────────────

    /**
     * Verify a 2FA OTP for a user.
     * Increments attempt count and blocks after MAX_ATTEMPTS failures.
     *
     * @return true if the code matches and is still valid
     */
    @Transactional
    public boolean verifyForUser(Long userId, String rawCode, OtpType type) {
        Optional<OtpCode> opt =
                otpRepository.findTopByUserIdAndTypeAndUsedFalseAndBlockedFalseAndExpiresAtAfter(
                        userId, type, LocalDateTime.now());

        if (opt.isEmpty()) return false;
        return checkAndUpdate(opt.get(), rawCode);
    }

    /**
     * Verify the withdrawal OTP for a transfer.
     *
     * @return true if the code matches and is still valid
     */
    @Transactional
    public boolean verifyForTransfer(Long transferId, String rawCode) {
        Optional<OtpCode> opt =
                otpRepository.findTopByTransferIdAndTypeAndUsedFalseAndBlockedFalseAndExpiresAtAfter(
                        transferId, OtpType.WITHDRAWAL_VERIFY, LocalDateTime.now());

        if (opt.isEmpty()) return false;
        return checkAndUpdate(opt.get(), rawCode);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private boolean checkAndUpdate(OtpCode otp, String rawCode) {
        otp.setAttemptCount(otp.getAttemptCount() + 1);

        if (otp.getAttemptCount() >= MAX_ATTEMPTS) {
            otp.setBlocked(true);
            otpRepository.save(otp);
            return false;
        }

        boolean matches = encoder.matches(rawCode, otp.getCodeHash());
        if (matches) {
            otp.setUsed(true);

            // Audit: log who verified successfully and what type
            Long auditUserId = otp.getUser() != null ? otp.getUser().getId() : null;
            Long auditTransferId = otp.getTransfer() != null ? otp.getTransfer().getId() : null;
            auditService.log(
                    auditUserId,
                    "OTP_VERIFIED",
                    auditTransferId != null ? "Transfer" : "User",
                    auditTransferId != null ? auditTransferId : auditUserId,
                    "type=" + otp.getType()
            );
        } else {
            // Audit failed attempts (security event)
            Long auditUserId = otp.getUser() != null ? otp.getUser().getId() : null;
            auditService.log(
                    auditUserId,
                    "OTP_FAILED",
                    "User",
                    auditUserId,
                    "type=" + otp.getType() + ", attempt=" + otp.getAttemptCount()
            );
        }
        otpRepository.save(otp);
        return matches;
    }

    private String generateNumericCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
