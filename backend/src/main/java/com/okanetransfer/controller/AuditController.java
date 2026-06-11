package com.okanetransfer.controller;

import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.AuditLogResponse;
import com.okanetransfer.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Audit", description = "Audit log management")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping
    @Operation(summary = "Get all audit logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Audit logs retrieved successfully",
                        auditService.getAll()));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs for a specific user")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByUser(
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success("Audit logs retrieved successfully",
                        auditService.getByUser(userId)));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit logs for a specific entity")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByEntity(
            @PathVariable("entityType") String entityType,
            @PathVariable("entityId") Long entityId) {
        return ResponseEntity.ok(
                ApiResponse.success("Audit logs retrieved successfully",
                        auditService.getByEntity(entityType, entityId)));
    }

    @GetMapping("/range")
    @Operation(summary = "Get audit logs within a date range")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByRange(
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(
                ApiResponse.success("Audit logs retrieved successfully",
                        auditService.getByDateRange(from, to)));
    }

    @GetMapping("/action/{action}")
    @Operation(summary = "Get audit logs by action type")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByAction(
            @PathVariable("action") String action) {
        return ResponseEntity.ok(
                ApiResponse.success("Audit logs retrieved successfully",
                        auditService.getByAction(action)));
    }
}