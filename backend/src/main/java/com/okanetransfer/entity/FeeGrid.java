package com.okanetransfer.entity;

import com.okanetransfer.entity.enums.TransferType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fee_grids")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeGrid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @NotNull
    @Column(name = "min_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal minAmount;

    @NotNull
    @Column(name = "max_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxAmount;

    // Either fixed fee or percentage fee (or both)
    @Column(name = "fee_fixed_amount", precision = 15, scale = 2)
    private BigDecimal feeFixedAmount;

    @Column(name = "fee_percentage", precision = 5, scale = 2)
    private BigDecimal feePercentage;

    @NotNull
    @Column(name = "agency_share_percent", nullable = false)
    private Integer agencySharePercent;

    @NotNull
    @Column(name = "central_share_percent", nullable = false)
    private Integer centralSharePercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false, length = 20)
    @Builder.Default
    private TransferType transferType = TransferType.STANDARD;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}