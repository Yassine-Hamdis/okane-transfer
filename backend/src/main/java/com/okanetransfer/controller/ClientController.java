package com.okanetransfer.controller;

import com.okanetransfer.dto.request.ChangePasswordRequest;
import com.okanetransfer.dto.request.UpdateProfileRequest;
import com.okanetransfer.dto.response.ClientProfileResponse;
import com.okanetransfer.dto.response.TransferSummaryResponse;
import com.okanetransfer.dto.response.TransferTrackResponse;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.ClientService;
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

@RestController
@RequestMapping("/api/client")
@PreAuthorize("hasRole('ROLE_CLIENT')")
@Tag(name = "Client", description = "Client self-service portal")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserRepository userRepository;

    // ── Profile ────────────────────────────────────────

    @GetMapping("/profile")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ClientProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                clientService.getMyProfile(resolveUserId(userDetails)));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update my profile")
    public ResponseEntity<ClientProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                clientService.updateProfile(resolveUserId(userDetails), request));
    }

    @PatchMapping("/profile/change-password")
    @Operation(summary = "Change my password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        clientService.changePassword(resolveUserId(userDetails), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/profile/toggle-2fa")
    @Operation(summary = "Enable or disable two-factor authentication")
    public ResponseEntity<Void> toggleTwoFactor(
            @AuthenticationPrincipal UserDetails userDetails) {
        clientService.toggleTwoFactor(resolveUserId(userDetails));
        return ResponseEntity.noContent().build();
    }

    // ── Transfers ──────────────────────────────────────

    @GetMapping("/transfers")
    @Operation(summary = "Get all my transfers")
    public ResponseEntity<List<TransferSummaryResponse>> getMyTransfers(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                clientService.getMyTransfers(resolveUserId(userDetails)));
    }

    @GetMapping("/transfers/{id}")
    @Operation(summary = "Get a specific transfer by ID")
    public ResponseEntity<TransferSummaryResponse> getTransferById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                clientService.getMyTransferById(resolveUserId(userDetails), id));
    }

    @GetMapping("/transfers/track/{code}")
    @Operation(summary = "Track a transfer by withdrawal code — public endpoint")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransferTrackResponse> trackTransfer(
            @PathVariable String code) {
        return ResponseEntity.ok(clientService.trackTransfer(code));
    }

    // ── Helper ─────────────────────────────────────────

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}