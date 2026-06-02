package com.okanetransfer.controller;

import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.NotificationResponse;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client/notifications")
@PreAuthorize("hasAnyAuthority('ROLE_CLIENT','ROLE_ADMIN')")
@Tag(name = "Notifications", description = "Client notification management")
public class NotificationController {

    @Autowired private NotificationService notificationService;
    @Autowired private UserRepository      userRepository;

    @GetMapping
    @Operation(summary = "Get all my notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Notifications retrieved successfully",
                        notificationService.getMyNotifications(
                                resolveUserId(userDetails))));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnread(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Unread notifications retrieved successfully",
                        notificationService.getUnread(resolveUserId(userDetails))));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countUnread(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = notificationService.countUnread(resolveUserId(userDetails));
        return ResponseEntity.ok(
                ApiResponse.success("Unread count retrieved successfully",
                        Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAsRead(id, resolveUserId(userDetails));
        return ResponseEntity.ok(
                ApiResponse.success("Notification marked as read"));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(resolveUserId(userDetails));
        return ResponseEntity.ok(
                ApiResponse.success("All notifications marked as read"));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}