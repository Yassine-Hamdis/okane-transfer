package com.okanetransfer.entity;

import com.okanetransfer.entity.enums.MobileMoneyStatus;
import com.okanetransfer.entity.enums.MobileOperator;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_money")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobileMoney {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Only exists when transfer.transferType = MOBILE_MONEY
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false, unique = true)
    private Transfer transfer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 20)
    private MobileOperator operator;

    @NotBlank
    @Size(max = 20)
    @Column(name = "wallet_phone", nullable = false, length = 20)
    private String walletPhone;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MobileMoneyStatus status = MobileMoneyStatus.PENDING;

    // Fake transaction ID returned by simulated API
    @Size(max = 100)
    @Column(name = "operator_reference", length = 100)
    private String operatorReference;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    // Set when admin confirms the operator received it
    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;
}