package com.okanetransfer.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long          id;
    private Long          userId;
    private String        userEmail;
    private String        action;
    private String        entityType;
    private Long          entityId;
    private String        details;
    private String        ipAddress;
    private LocalDateTime createdAt;
}