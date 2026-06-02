package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateMobileMoneyRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.MobileMoneyResponse;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.MobileMoneyService;
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
@Tag(name = "Mobile Money", description = "Mobile money transfer management")
public class MobileMoneyController {

    @Autowired private MobileMoneyService mobileMoneyService;
    @Autowired private UserRepository     userRepository;

    @PostMapping("/api/agent/mobile-money")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    @Operation(summary = "Initiate a mobile money transfer")
    public ResponseEntity<ApiResponse<MobileMoneyResponse>> initiate(
            @Valid @RequestBody CreateMobileMoneyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Mobile money transfer initiated successfully",
                        mobileMoneyService.initiate(request,
                                resolveUserId(userDetails))));
    }

    @GetMapping("/api/agent/mobile-money/transfer/{transferId}")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    @Operation(summary = "Get mobile money record for a transfer")
    public ResponseEntity<ApiResponse<MobileMoneyResponse>> getByTransfer(
            @PathVariable Long transferId) {
        return ResponseEntity.ok(
                ApiResponse.success("Mobile money record retrieved successfully",
                        mobileMoneyService.getByTransfer(transferId)));
    }

    @GetMapping("/api/admin/mobile-money/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all pending mobile money transfers")
    public ResponseEntity<ApiResponse<List<MobileMoneyResponse>>> getPending() {
        return ResponseEntity.ok(
                ApiResponse.success("Pending mobile money transfers retrieved",
                        mobileMoneyService.getPending()));
    }

    @GetMapping("/api/admin/mobile-money/sent")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all sent mobile money transfers")
    public ResponseEntity<ApiResponse<List<MobileMoneyResponse>>> getSent() {
        return ResponseEntity.ok(
                ApiResponse.success("Sent mobile money transfers retrieved",
                        mobileMoneyService.getSent()));
    }

    @PatchMapping("/api/admin/mobile-money/{id}/reconcile")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Reconcile a mobile money transfer")
    public ResponseEntity<ApiResponse<MobileMoneyResponse>> reconcile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Mobile money transfer reconciled successfully",
                        mobileMoneyService.reconcile(id, resolveUserId(userDetails))));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}