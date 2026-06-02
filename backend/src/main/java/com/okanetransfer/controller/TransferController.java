package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CancelTransferRequest;
import com.okanetransfer.dto.request.CreateTransferRequest;
import com.okanetransfer.dto.request.PayoutRequest;
import com.okanetransfer.dto.response.TransferResponse;
import com.okanetransfer.entity.enums.TransferStatus;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.TransferService;
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
@Tag(name = "Transfers", description = "Transfer management")
public class TransferController {

    @Autowired private TransferService transferService;
    @Autowired private UserRepository  userRepository;

    // ── Agent endpoints ────────────────────────────────

    @PostMapping("/api/agent/transfers")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER','ADMIN')")
    @Operation(summary = "Create a new transfer")
    public ResponseEntity<TransferResponse> create(
            @Valid @RequestBody CreateTransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transferService.createTransfer(request, resolveUserId(userDetails)));
    }

    @PostMapping("/api/agent/transfers/payout")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER','ADMIN')")
    @Operation(summary = "Pay out a transfer to recipient")
    public ResponseEntity<TransferResponse> payout(
            @Valid @RequestBody PayoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transferService.processPayment(request, resolveUserId(userDetails)));
    }

    @GetMapping("/api/agent/transfers/{id}")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER','ADMIN')")
    @Operation(summary = "Get transfer by ID")
    public ResponseEntity<TransferResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getById(id));
    }

    @GetMapping("/api/agent/transfers/code/{code}")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER','ADMIN')")
    @Operation(summary = "Get transfer by withdrawal code")
    public ResponseEntity<TransferResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(transferService.getByWithdrawalCode(code));
    }

    @GetMapping("/api/agent/transfers/my")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER','ADMIN')")
    @Operation(summary = "Get my transfers as agent")
    public ResponseEntity<List<TransferResponse>> getMyTransfers(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transferService.getMyTransfersAsAgent(resolveUserId(userDetails)));
    }

    @GetMapping("/api/agent/transfers/search")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER','ADMIN')")
    @Operation(summary = "Search transfers by recipient phone")
    public ResponseEntity<List<TransferResponse>> searchByPhone(
            @RequestParam String phone) {
        return ResponseEntity.ok(transferService.searchByRecipientPhone(phone));
    }

    @PatchMapping("/api/agent/transfers/{id}/cancel")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER','ADMIN')")
    @Operation(summary = "Cancel a transfer")
    public ResponseEntity<TransferResponse> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelTransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transferService.cancelTransfer(id, request, resolveUserId(userDetails)));
    }

    // ── Admin endpoints ────────────────────────────────

    @GetMapping("/api/admin/transfers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all transfers")
    public ResponseEntity<List<TransferResponse>> getAll() {
        return ResponseEntity.ok(transferService.getAll());
    }

    @GetMapping("/api/admin/transfers/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get transfers by status")
    public ResponseEntity<List<TransferResponse>> getByStatus(
            @PathVariable TransferStatus status) {
        return ResponseEntity.ok(transferService.getByStatus(status));
    }

    @PatchMapping("/api/admin/transfers/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a blocked transfer")
    public ResponseEntity<TransferResponse> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                transferService.approveTransfer(id, resolveUserId(userDetails)));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}