package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "cash_register_balances",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"cash_register_id", "currency_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashRegisterBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_register_id", nullable = false)
    private CashRegister cashRegister;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @NotNull
    @Column(name = "current_balance", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}