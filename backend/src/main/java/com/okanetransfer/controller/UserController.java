package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateUserRequest;
import com.okanetransfer.dto.request.UpdateUserRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.UserResponse;
import com.okanetransfer.entity.enums.Role;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "User Management", description = "Admin user management")
public class UserController {

    @Autowired private UserService    userService;
    @Autowired private UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully",
                        userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getById(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully",
                        userService.getUserById(id)));
    }

    @GetMapping("/agency/{agencyId}")
    @Operation(summary = "Get users by agency")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getByAgency(
            @PathVariable("agencyId") Long agencyId) {
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully",
                        userService.getUsersByAgency(agencyId)));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getByRole(
            @PathVariable("role") Role role) {
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully",
                        userService.getUsersByRole(role)));
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("User created successfully",
                        userService.createUser(request, resolveUserId(userDetails))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user info")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully",
                        userService.updateUser(id, request, resolveUserId(userDetails))));
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspend a user")
    public ResponseEntity<ApiResponse<Void>> suspend(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.suspendUser(id, resolveUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("User suspended successfully"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a suspended user")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.activateUser(id, resolveUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("User activated successfully"));
    }

    @PatchMapping("/{id}/reset-password")
    @Operation(summary = "Reset a user's password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("newPassword is required");
        }

        userService.resetPassword(id, newPassword, resolveUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}