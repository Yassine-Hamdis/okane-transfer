package com.okanetransfer.service;

import com.okanetransfer.entity.OtpCode;
import com.okanetransfer.entity.Transfer;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.OtpType;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.OtpCodeRepository;
import com.okanetransfer.repository.TransferRepository;
import com.okanetransfer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final int OTP_LENGTH      = 6;
    private static final int OTP_VALIDITY_MIN = 5;   // 5 minutes
    private static final int MAX_ATTEMPTS    = 5;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired private OtpCodeRepository  otpCodeRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private TransferRepository transferRepository;
    @Autowired private PasswordEncoder    passwordEncoder;

    // ─────────────────────────────────────────────────────
    //  GENERATE — for user 2FA
    // ─────────────────────────────────────────────────────

    /**
     * Generates a 6-digit OTP for user 2FA login.
     * Hashes and saves to DB.
     *
     * @return plain OTP — send to user via SMS, never store plain
     */
    @Transactional
    public String generateAndSave(Long userId, OtpType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String plain = generateRawOtp();

        OtpCode otp = OtpCode.builder()
                .user(user)
                .transfer(null)
                .codeHash(passwordEncoder.encode(plain))
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MIN))
                .used(false)
                .attemptCount(0)
                .blocked(false)
                .build();

        otpCodeRepository.save(otp);
        return plain;
    }

    // ─────────────────────────────────────────────────────
    //  GENERATE — for transfer withdrawal verification
    // ─────────────────────────────────────────────────────

    /**
     * Generates a 6-digit OTP for verifying a withdrawal at the agency.
     *
     * @return plain OTP — agent gives it to recipient verbally or via SMS
     */
    @Transactional
    public String generateForTransfer(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transfer", transferId));

        String plain = generateRawOtp();

        OtpCode otp = OtpCode.builder()
                .user(null)
                .transfer(transfer)
                .codeHash(passwordEncoder.encode(plain))
                .type(OtpType.WITHDRAWAL_VERIFY)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MIN))
                .used(false)
                .attemptCount(0)
                .blocked(false)
                .build();

        otpCodeRepository.save(otp);
        return plain;
    }

    // ─────────────────────────────────────────────────────
    //  VERIFY — for user 2FA
    // ─────────────────────────────────────────────────────

    /**
     * Verifies an OTP for a user.
     *
     * @return true if valid, false if wrong/expired/blocked
     */
    @Transactional
    public boolean verify(Long userId, String rawCode, OtpType type) {
        Optional<OtpCode> otpOpt =
                otpCodeRepository
                        .findTopByUserIdAndTypeAndUsedFalseAndBlockedFalseAndExpiresAtAfter(
                                userId, type, LocalDateTime.now());

        if (otpOpt.isEmpty()) {
            log.warn("No valid OTP found for userId={}", userId);
            return false;
        }

        OtpCode otp = otpOpt.get();
        return validateAndConsume(otp, rawCode);
    }

    // ─────────────────────────────────────────────────────
    //  VERIFY — for transfer withdrawal
    // ─────────────────────────────────────────────────────

    @Transactional
    public boolean verifyForTransfer(Long transferId, String rawCode) {
        Optional<OtpCode> otpOpt =
                otpCodeRepository
                        .findTopByTransferIdAndTypeAndUsedFalseAndBlockedFalseAndExpiresAtAfter(
                                transferId,
                                OtpType.WITHDRAWAL_VERIFY,
                                LocalDateTime.now());

        if (otpOpt.isEmpty()) {
            log.warn("No valid OTP for transferId={}", transferId);
            return false;
        }

        return validateAndConsume(otpOpt.get(), rawCode);
    }

    // ─────────────────────────────────────────────────────
    //  PRIVATE
    // ─────────────────────────────────────────────────────

    private boolean validateAndConsume(OtpCode otp, String rawCode) {
        // Increment attempt count
        otp.setAttemptCount(otp.getAttemptCount() + 1);

        // Block after max attempts
        if (otp.getAttemptCount() >= MAX_ATTEMPTS) {
            otp.setBlocked(true);
            otpCodeRepository.save(otp);
            log.warn("OTP id={} blocked after {} attempts", otp.getId(), MAX_ATTEMPTS);
            return false;
        }

        // Check code
        if (!passwordEncoder.matches(rawCode, otp.getCodeHash())) {
            otpCodeRepository.save(otp);
            log.warn("Wrong OTP for id={} attempt={}", otp.getId(), otp.getAttemptCount());
            return false;
        }

        // Valid — mark as used
        otp.setUsed(true);
        otpCodeRepository.save(otp);
        return true;
    }

    private String generateRawOtp() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
}