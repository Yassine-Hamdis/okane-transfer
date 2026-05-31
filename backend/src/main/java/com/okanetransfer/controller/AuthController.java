package com.okanetransfer.controller;

import com.okanetransfer.dto.request.LoginRequest;
import com.okanetransfer.dto.request.RegisterRequest;
import com.okanetransfer.dto.request.Verify2FaRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.AuthResponse;
import com.okanetransfer.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login processed", authService.login(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registered successfully", null));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<ApiResponse<AuthResponse>> verify2fa(@Valid @RequestBody Verify2FaRequest request) {
        return ResponseEntity.ok(ApiResponse.success("2FA verified", authService.verify2fa(request)));
    }
}