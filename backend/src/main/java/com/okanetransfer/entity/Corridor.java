package com.okanetransfer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "corridors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Corridor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_country_id", nullable = false)
    private Country sourceCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_country_id", nullable = false)
    private Country destinationCountry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_currency_id", nullable = false)
    private Currency sourceCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_currency_id", nullable = false)
    private Currency destinationCurrency;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}