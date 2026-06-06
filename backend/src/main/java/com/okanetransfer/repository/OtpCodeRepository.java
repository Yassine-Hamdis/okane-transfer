package com.okanetransfer.repository;

import com.okanetransfer.entity.OtpCode;
import com.okanetransfer.entity.enums.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    // Latest valid OTP for a user (2FA)
    Optional<OtpCode> findTopByUserIdAndTypeAndUsedFalseAndBlockedFalseAndExpiresAtAfter(
            Long userId, OtpType type, LocalDateTime now);

    // Latest valid OTP for a transfer (withdrawal verify)
    Optional<OtpCode> findTopByTransferIdAndTypeAndUsedFalseAndBlockedFalseAndExpiresAtAfter(
            Long transferId, OtpType type, LocalDateTime now);

    Optional<OtpCode> findTopByUserIdAndTypeOrderByCreatedAtDesc(Long userId, OtpType type);

    List<OtpCode> findAllByUserId(Long userId);
    List<OtpCode> findAllByTransferId(Long transferId);
}