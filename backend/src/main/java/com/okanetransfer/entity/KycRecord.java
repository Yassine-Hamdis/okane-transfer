package com.okanetransfer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_records")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class KycRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transfer_id", nullable = false)
    private Transfer transfer;

    // "PASSED", "FLAGGED", "BLOCKED"
    @Column(nullable = false)
    private String status;

    @Column(name = "watchlist_hit")
    private boolean watchlistHit = false;

    @Column(name = "suspicion_declared")
    private boolean suspicionDeclared = false;

    @Column(name = "risk_score")
    private Integer riskScore; // 0-100

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @PrePersist
    protected void onCreate() {
        checkedAt = LocalDateTime.now();
    }
}