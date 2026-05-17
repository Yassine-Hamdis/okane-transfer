package com.okanetransfer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cash_registers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CashRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @Column(name = "current_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "last_closed_at")
    private LocalDateTime lastClosedAt;

    @OneToMany(mappedBy = "cashRegister", cascade = CascadeType.ALL)
    private List<CashOperation> operations;
}