package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateUserRequest;
import com.okanetransfer.dto.request.UpdateUserRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.UserResponse;
import com.okanetransfer.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) { this.userService = userService; }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("OK", userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("OK", userService.getUserById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Created", userService.createUser(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Updated", userService.updateUser(id, request)));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<Object>> suspend(@PathVariable Long id) {
        userService.suspendUser(id);
        return ResponseEntity.ok(ApiResponse.success("Suspended", null));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Object>> activate(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("Activated", null));
    }

    @GetMapping("/agency/{agencyId}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> byAgency(@PathVariable Long agencyId) {
        return ResponseEntity.ok(ApiResponse.success("OK", userService.getUsersByAgency(agencyId)));
    }
}