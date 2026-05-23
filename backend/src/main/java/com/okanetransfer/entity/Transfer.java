package com.okanetransfer.entity;

import com.okanetransfer.entity.enums.TransferStatus;
import com.okanetransfer.entity.enums.TransferType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 8)
    @Column(name = "withdrawal_code", nullable = false, unique = true, length = 8)
    private String withdrawalCode;

    // ── Sender ──────────────────────────────────────────
    @NotBlank
    @Size(max = 100)
    @Column(name = "sender_first_name", nullable = false, length = 100)
    private String senderFirstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "sender_last_name", nullable = false, length = 100)
    private String senderLastName;

    @NotBlank
    @Size(max = 20)
    @Column(name = "sender_phone", nullable = false, length = 20)
    private String senderPhone;

    // AES-256 encrypted
    @Size(max = 500)
    @Column(name = "sender_id_encrypted", length = 500)
    private String senderIdEncrypted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_country_id", nullable = false)
    private Country senderCountry;

    // ── Recipient ────────────────────────────────────────
    @NotBlank
    @Size(max = 100)
    @Column(name = "recipient_first_name", nullable = false, length = 100)
    private String recipientFirstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "recipient_last_name", nullable = false, length = 100)
    private String recipientLastName;

    @NotBlank
    @Size(max = 20)
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    // AES-256 encrypted — filled at payout time
    @Size(max = 500)
    @Column(name = "recipient_id_encrypted", length = 500)
    private String recipientIdEncrypted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_country_id", nullable = false)
    private Country recipientCountry;

    // ── Amounts ──────────────────────────────────────────
    @NotNull
    @Column(name = "sent_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal sentAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sent_currency_id", nullable = false)
    private Currency sentCurrency;

    @Column(name = "fee_fixed", precision = 15, scale = 2)
    private BigDecimal feeFixed;

    @Column(name = "fee_percentage", precision = 5, scale = 2)
    private BigDecimal feePercentage;

    @NotNull
    @Column(name = "fee_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal feeAmount;

    @NotNull
    @Column(name = "received_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal receivedAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_currency_id", nullable = false)
    private Currency receivedCurrency;

    @NotNull
    @Column(name = "exchange_rate", nullable = false, precision = 15, scale = 6)
    private BigDecimal exchangeRate;

    // ── Type & Status ────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false, length = 20)
    @Builder.Default
    private TransferType transferType = TransferType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TransferStatus status = TransferStatus.EN_ATTENTE;

    @Column(name = "requires_admin_approval", nullable = false)
    @Builder.Default
    private boolean requiresAdminApproval = false;

    @Column(name = "blocked_reason", columnDefinition = "TEXT")
    private String blockedReason;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ── Relations ────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sending_agency_id", nullable = false)
    private Agency sendingAgency;

    // NULL until recipient agent picks it up
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_agency_id", nullable = true)
    private Agency receivingAgency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sending_agent_id", nullable = false)
    private User sendingAgent;

    // NULL until payout
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_agent_id", nullable = true)
    private User receivingAgent;

    // NULL if sender has no online account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = true)
    private User client;

    // ── Dates ────────────────────────────────────────────
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusDays(30);
    }
}