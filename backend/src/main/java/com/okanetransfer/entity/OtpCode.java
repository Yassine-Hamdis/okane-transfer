package com.okanetransfer.entity;

import com.okanetransfer.entity.enums.OtpType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Filled for TWO_FACTOR type
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // Filled for WITHDRAWAL_VERIFY type
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = true)
    private Transfer transfer;

    // BCrypt hashed — NEVER stored in plain text
    @NotBlank
    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private OtpType type;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private boolean used = false;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private int attemptCount = 0;

    // Blocked after attemptCount >= 5
    @Column(name = "blocked", nullable = false)
    @Builder.Default
    private boolean blocked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}