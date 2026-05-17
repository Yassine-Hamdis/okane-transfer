package com.okanetransfer.entity;

import com.okanetransfer.enums.OperationType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_operations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CashOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cash_register_id", nullable = false)
    private CashRegister cashRegister;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    @ManyToOne
    @JoinColumn(name = "transfer_id")
    private Transfer transfer; // nullable (e.g. for cash discrepancy reports)

    @Column(nullable = true)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}