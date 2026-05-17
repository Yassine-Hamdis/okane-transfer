package com.okanetransfer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    @Column(nullable = false, precision = 15, scale = 6)
    private BigDecimal rate;

    // "MANUAL" or "API"
    @Column(nullable = false)
    private String source;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }
}