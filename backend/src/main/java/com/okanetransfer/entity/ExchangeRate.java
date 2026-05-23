package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corridor_id", nullable = false)
    private Corridor corridor;

    @NotNull
    @Column(name = "rate", nullable = false, precision = 15, scale = 6)
    private BigDecimal rate;

    @Column(name = "source", nullable = false, length = 10)
    private String source;

    // NULL when rate comes from external API automatically
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = true)
    private User updatedBy;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private boolean isCurrent = false;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        this.recordedAt = LocalDateTime.now();
    }
}