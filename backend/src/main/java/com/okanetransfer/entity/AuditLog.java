package com.okanetransfer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who did the action
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // What action was performed
    @Column(nullable = false)
    private String action;       // e.g. "TRANSFER_CREATED", "USER_DELETED"

    // Which entity was affected
    @Column(name = "entity_type")
    private String entityType;   // e.g. "Transfer", "User"

    @Column(name = "entity_id")
    private Long entityId;

    // Extra details (JSON string)
    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}