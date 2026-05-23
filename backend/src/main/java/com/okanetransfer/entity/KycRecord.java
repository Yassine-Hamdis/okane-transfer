package com.okanetransfer.entity;

import com.okanetransfer.entity.enums.KycStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private Transfer transfer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private KycStatus status;

    @Column(name = "watchlist_hit", nullable = false)
    @Builder.Default
    private boolean watchlistHit = false;

    @Column(name = "suspicion_declared", nullable = false)
    @Builder.Default
    private boolean suspicionDeclared = false;

    // Risk score 0 to 100
    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // NULL if auto-checked only (no human review)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checked_by", nullable = true)
    private User checkedBy;

    @NotNull
    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;
}