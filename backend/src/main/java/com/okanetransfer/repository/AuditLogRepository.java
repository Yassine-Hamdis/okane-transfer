package com.okanetransfer.repository;

import com.okanetransfer.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByUserId(Long userId);
    List<AuditLog> findAllByAction(String action);
    List<AuditLog> findAllByEntityTypeAndEntityId(String entityType, Long entityId);
    List<AuditLog> findAllByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime to);
}