package com.okanetransfer.controller;

import com.okanetransfer.dto.request.ChangePasswordRequest;
import com.okanetransfer.dto.request.UpdateProfileRequest;
import com.okanetransfer.dto.response.ApiResponse;
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
@Tag(name = "Client", description = "Client self-service portal")
public class ClientController {

    @Autowired private ClientService  clientService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENT','ROLE_ADMIN')")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<ClientProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully",
                        clientService.getMyProfile(resolveUserId(userDetails))));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENT','ROLE_ADMIN')")
    @Operation(summary = "Update my profile")
    public ResponseEntity<ApiResponse<ClientProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully",
                        clientService.updateProfile(
                                resolveUserId(userDetails), request)));
    }

    @PatchMapping("/profile/change-password")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENT','ROLE_ADMIN')")
    @Operation(summary = "Change my password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        clientService.changePassword(resolveUserId(userDetails), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @PatchMapping("/profile/toggle-2fa")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENT','ROLE_ADMIN')")
    @Operation(summary = "Enable or disable two-factor authentication")
    public ResponseEntity<ApiResponse<Void>> toggleTwoFactor(
            @AuthenticationPrincipal UserDetails userDetails) {
        clientService.toggleTwoFactor(resolveUserId(userDetails));
        return ResponseEntity.ok(
                ApiResponse.success("Two-factor authentication toggled successfully"));
    }

    @GetMapping("/transfers")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENT','ROLE_ADMIN')")
    @Operation(summary = "Get all my transfers")
    public ResponseEntity<ApiResponse<List<TransferSummaryResponse>>> getTransfers(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfers retrieved successfully",
                        clientService.getMyTransfers(resolveUserId(userDetails))));
    }

    @GetMapping("/transfers/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_CLIENT','ROLE_ADMIN')")
    @Operation(summary = "Get a specific transfer by ID")
    public ResponseEntity<ApiResponse<TransferSummaryResponse>> getTransferById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfer retrieved successfully",
                        clientService.getMyTransferById(
                                resolveUserId(userDetails), id)));
    }

    @GetMapping("/transfers/track/{code}")
    @Operation(summary = "Track a transfer by withdrawal code")
    public ResponseEntity<ApiResponse<TransferTrackResponse>> track(
            @PathVariable("code") String code) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfer found",
                        clientService.trackTransfer(code)));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}