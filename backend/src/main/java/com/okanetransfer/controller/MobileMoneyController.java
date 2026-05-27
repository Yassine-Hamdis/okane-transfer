package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateMobileMoneyRequest;
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

    @Autowired
    private MobileMoneyService mobileMoneyService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/api/agent/mobile-money")
    @PreAuthorize("hasAnyRole('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    @Operation(summary = "Initiate a mobile money transfer")
    public ResponseEntity<MobileMoneyResponse> initiate(
            @Valid @RequestBody CreateMobileMoneyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long agentId = resolveUserId(userDetails);
        return ResponseEntity.ok(mobileMoneyService.initiate(request, agentId));
    }

    @GetMapping("/api/agent/mobile-money/transfer/{transferId}")
    @PreAuthorize("hasAnyRole('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    @Operation(summary = "Get mobile money record for a transfer")
    public ResponseEntity<MobileMoneyResponse> getByTransfer(
            @PathVariable Long transferId) {
        return ResponseEntity.ok(mobileMoneyService.getByTransfer(transferId));
    }

    @GetMapping("/api/admin/mobile-money/pending")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all pending mobile money transfers")
    public ResponseEntity<List<MobileMoneyResponse>> getPending() {
        return ResponseEntity.ok(mobileMoneyService.getPending());
    }

    @GetMapping("/api/admin/mobile-money/sent")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all sent (awaiting reconciliation) transfers")
    public ResponseEntity<List<MobileMoneyResponse>> getSent() {
        return ResponseEntity.ok(mobileMoneyService.getSent());
    }

    @PatchMapping("/api/admin/mobile-money/{id}/reconcile")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Reconcile a mobile money transfer")
    public ResponseEntity<MobileMoneyResponse> reconcile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = resolveUserId(userDetails);
        return ResponseEntity.ok(mobileMoneyService.reconcile(id, adminId));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}