package com.okanetransfer.entity;

import com.okanetransfer.entity.enums.OperationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_operations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_register_id", nullable = false)
    private CashRegister cashRegister;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private OperationType type;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    // Added: which currency this operation is in
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    // NULL for CLOTURE_CAISSE and manual discrepancy reports
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = true)
    private Transfer transfer;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}