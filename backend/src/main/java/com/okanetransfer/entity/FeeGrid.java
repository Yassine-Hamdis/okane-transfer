package com.okanetransfer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fee_grids")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FeeGrid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    // Amount range: from minAmount to maxAmount
    @Column(name = "min_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxAmount;

    // Total fee for this range
    @Column(name = "fee_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal feeAmount;

    // What percentage goes to the agency
    @Column(name = "agency_share_percent", nullable = false)
    private Integer agencySharePercent;

    @Column(nullable = false)
    private boolean active = true;
}