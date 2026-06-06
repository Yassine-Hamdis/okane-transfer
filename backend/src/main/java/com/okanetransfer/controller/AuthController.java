package com.okanetransfer.controller;

import com.okanetransfer.dto.request.LoginRequest;
import com.okanetransfer.dto.request.RegisterRequest;
import com.okanetransfer.dto.request.TwoFactorRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.LoginResponse;
import com.okanetransfer.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, register, 2FA")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        LoginResponse response = authService.login(
                request, httpRequest.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Self-register as a client")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);
        return ResponseEntity.ok(
                ApiResponse.success("Account created successfully. You can now login."));
    }

    @PostMapping("/verify-2fa")
    @Operation(summary = "Verify 2FA OTP code to complete login")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyTwoFactor(
            @Valid @RequestBody TwoFactorRequest request,
            HttpServletRequest httpRequest) {

        LoginResponse response = authService.verifyTwoFactor(
                request, httpRequest.getRemoteAddr());

        return ResponseEntity.ok(
                ApiResponse.success("2FA verified successfully", response));
    }
}