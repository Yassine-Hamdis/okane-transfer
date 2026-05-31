package com.okanetransfer.controller;

import com.okanetransfer.dto.request.RegisterRequest;
import com.okanetransfer.dto.response.AuthResponse;
import com.okanetransfer.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Le service s'occupe de la sauvegarde et génère le JWT
        AuthResponse response = authService.registerClient(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        String message = authService.verifyEmail(token);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<String> verifyPhone(@RequestParam("phone") String phone, @RequestParam("otp") String otp) {
        String message = authService.verifyPhone(phone, otp);
        return ResponseEntity.ok(message);
    }
}