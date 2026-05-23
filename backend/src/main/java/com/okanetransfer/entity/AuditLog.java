package com.okanetransfer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NULL for anonymous actions (e.g. LOGIN_FAILED before auth)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @NotBlank
    @Size(max = 100)
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Size(max = 50)
    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    // JSON string for extra context
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Size(max = 45)
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}