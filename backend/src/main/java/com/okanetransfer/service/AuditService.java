package com.okanetransfer.service;

import com.okanetransfer.dto.response.AuditLogResponse;
import com.okanetransfer.entity.AuditLog;
import com.okanetransfer.entity.User;
import com.okanetransfer.repository.AuditLogRepository;
import com.okanetransfer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    // ─────────────────────────────────────────────────────
    //  CORE LOG METHOD — called by every other service
    // ─────────────────────────────────────────────────────

    /**
     * Logs a sensitive action.
     * Called internally by other services — never by controllers directly.
     *
     * @param userId     the user who performed the action (null = anonymous)
     * @param action     e.g. "TRANSFER_CREATED", "USER_SUSPENDED"
     * @param entityType e.g. "Transfer", "User", "Agency" (null if not applicable)
     * @param entityId   the ID of the affected entity (null if not applicable)
     * @param details    any extra context as JSON string (null if not needed)
     * @param ipAddress  request IP (null if not available)
     */
    @Transactional
    public void log(Long userId,
                    String action,
                    String entityType,
                    Long entityId,
                    String details,
                    String ipAddress) {
        try {
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }

            AuditLog entry = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(entry);

        } catch (Exception e) {
            // Audit logging must NEVER break the main flow
            log.error("Failed to write audit log — action={}, userId={}: {}",
                    action, userId, e.getMessage());
        }
    }

    /**
     * Shortcut — log without IP address.
     * Use this when calling from service layer (no request context).
     */
    @Transactional
    public void log(Long userId,
                    String action,
                    String entityType,
                    Long entityId,
                    String details) {
        log(userId, action, entityType, entityId, details, null);
    }

    /**
     * Shortcut — log a simple action with no entity context.
     * e.g. "LOGIN_FAILED"
     */
    @Transactional
    public void log(Long userId, String action, String ipAddress) {
        log(userId, action, null, null, null, ipAddress);
    }

    // ─────────────────────────────────────────────────────
    //  QUERY METHODS — used by AuditController
    // ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAll() {
        return auditLogRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByUser(Long userId) {
        return auditLogRepository.findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByEntity(String entityType, Long entityId) {
        return auditLogRepository.findAllByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByDateRange(LocalDateTime from, LocalDateTime to) {
        return auditLogRepository.findAllByCreatedAtBetweenOrderByCreatedAtDesc(from, to)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByAction(String action) {
        return auditLogRepository.findAllByAction(action)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────
    //  MAPPER
    // ─────────────────────────────────────────────────────

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userEmail(log.getUser() != null ? log.getUser().getEmail() : null)
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}