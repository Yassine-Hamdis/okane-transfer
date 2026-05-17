package com.okanetransfer.entity;

import com.okanetransfer.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique withdrawal code (8 chars, alphanumeric)
    @Column(name = "withdrawal_code", nullable = false, unique = true, length = 8)
    private String withdrawalCode;

    // ===== Sender info =====
    @Column(name = "sender_first_name", nullable = false)
    private String senderFirstName;

    @Column(name = "sender_last_name", nullable = false)
    private String senderLastName;

    @Column(name = "sender_phone", nullable = false)
    private String senderPhone;

    @Column(name = "sender_id_encrypted")
    private String senderIdEncrypted;

    @Column(name = "sender_country", nullable = false)
    private String senderCountry;

    // ===== Recipient info =====
    @Column(name = "recipient_first_name", nullable = false)
    private String recipientFirstName;

    @Column(name = "recipient_last_name", nullable = false)
    private String recipientLastName;

    @Column(name = "recipient_phone", nullable = false)
    private String recipientPhone;

    @Column(name = "recipient_country", nullable = false)
    private String recipientCountry;

    @Column(name = "recipient_id_encrypted")
    private String recipientIdEncrypted; // filled at payout

    // ===== Amounts =====
    // Amount the sender pays (before fees)
    @Column(name = "sent_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal sentAmount;

    // Fees charged
    @Column(name = "fee_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal feeAmount;

    // Amount the recipient receives (after conversion)
    @Column(name = "received_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal receivedAmount;

    // Exchange rate used
    @Column(name = "exchange_rate", nullable = false, precision = 15, scale = 6)
    private BigDecimal exchangeRate;

    // ===== Relations =====
    @ManyToOne
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    @ManyToOne
    @JoinColumn(name = "sending_agency_id", nullable = false)
    private Agency sendingAgency;

    @ManyToOne
    @JoinColumn(name = "receiving_agency_id")
    private Agency receivingAgency; // set at payout

    @ManyToOne
    @JoinColumn(name = "sending_agent_id", nullable = false)
    private User sendingAgent;

    @ManyToOne
    @JoinColumn(name = "receiving_agent_id")
    private User receivingAgent; // set at payout

    // The registered client account (optional — if sender has account)
    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    // ===== Status =====
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status = TransferStatus.EN_ATTENTE;

    // For Mobile Money transfers
    @OneToOne(mappedBy = "transfer", cascade = CascadeType.ALL)
    private MobileMoney mobileMoney;

    // ===== Dates =====
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // auto-set to createdAt + 30 days

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusDays(30);
    }
}