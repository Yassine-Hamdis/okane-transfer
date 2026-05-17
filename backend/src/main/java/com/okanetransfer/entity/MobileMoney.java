package com.okanetransfer.entity;

import com.okanetransfer.enums.MobileOperator;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_money")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MobileMoney {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "transfer_id", nullable = false)
    private Transfer transfer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MobileOperator operator;

    @Column(name = "wallet_phone", nullable = false)
    private String walletPhone;

    // "PENDING", "SENT", "RECONCILED", "FAILED"
    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "operator_reference")
    private String operatorReference; // fake transaction ID from operator

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;
}