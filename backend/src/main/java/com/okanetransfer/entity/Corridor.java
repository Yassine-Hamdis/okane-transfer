package com.okanetransfer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "corridors")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Corridor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. "MA" for Morocco
    @Column(name = "source_country", nullable = false, length = 3)
    private String sourceCountry;

    // e.g. "SN" for Senegal
    @Column(name = "destination_country", nullable = false, length = 3)
    private String destinationCountry;

    @ManyToOne
    @JoinColumn(name = "source_currency_id", nullable = false)
    private Currency sourceCurrency;

    @ManyToOne
    @JoinColumn(name = "destination_currency_id", nullable = false)
    private Currency destinationCurrency;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}