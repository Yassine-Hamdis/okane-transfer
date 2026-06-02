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

    @PrePersist
    @PreUpdate
    private void validate() {

        if (minAmount != null && maxAmount != null
                && minAmount.compareTo(maxAmount) >= 0) {
            throw new IllegalStateException("minAmount must be < maxAmount");
        }

        if (feeFixedAmount != null && feeFixedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("feeFixedAmount must be >= 0");
        }

        if (feePercentage != null &&
                (feePercentage.compareTo(BigDecimal.ZERO) < 0 ||
                        feePercentage.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalStateException("feePercentage must be between 0 and 100");
        }

        if (agencySharePercent + centralSharePercent != 100) {
            throw new IllegalStateException("Revenue split must equal 100%");
        }
    }
}