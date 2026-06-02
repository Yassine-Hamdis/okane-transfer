package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CancelTransferRequest;
import com.okanetransfer.dto.request.CreateTransferRequest;
import com.okanetransfer.dto.request.PayoutRequest;
import com.okanetransfer.dto.response.ApiResponse;
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

    @PostMapping("/api/agent/transfers")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    @Operation(summary = "Create a new transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> create(
            @Valid @RequestBody CreateTransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfer created successfully",
                        transferService.createTransfer(
                                request, resolveUserId(userDetails))));
    }

    @PostMapping("/api/agent/transfers/payout")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    @Operation(summary = "Pay out a transfer to recipient")
    public ResponseEntity<ApiResponse<TransferResponse>> payout(
            @Valid @RequestBody PayoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfer paid out successfully",
                        transferService.processPayment(
                                request, resolveUserId(userDetails))));
    }

    @GetMapping("/api/agent/transfers/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    @Operation(summary = "Get transfer by ID")
    public ResponseEntity<ApiResponse<TransferResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfer retrieved successfully",
                        transferService.getById(id)));
    }

    @GetMapping("/api/agent/transfers/code/{code}")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    @Operation(summary = "Get transfer by withdrawal code")
    public ResponseEntity<ApiResponse<TransferResponse>> getByCode(
            @PathVariable String code) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfer retrieved successfully",
                        transferService.getByWithdrawalCode(code)));
    }

    @GetMapping("/api/agent/transfers/my")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    @Operation(summary = "Get my transfers as agent")
    public ResponseEntity<ApiResponse<List<TransferResponse>>> getMyTransfers(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfers retrieved successfully",
                        transferService.getMyTransfersAsAgent(
                                resolveUserId(userDetails))));
    }

    @GetMapping("/api/agent/transfers/search")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    @Operation(summary = "Search transfers by recipient phone")
    public ResponseEntity<ApiResponse<List<TransferResponse>>> searchByPhone(
            @RequestParam String phone) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfers retrieved successfully",
                        transferService.searchByRecipientPhone(phone)));
    }

    @PatchMapping("/api/agent/transfers/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    @Operation(summary = "Cancel a transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelTransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfer cancelled successfully",
                        transferService.cancelTransfer(
                                id, request, resolveUserId(userDetails))));
    }

    @GetMapping("/api/admin/transfers")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all transfers")
    public ResponseEntity<ApiResponse<List<TransferResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Transfers retrieved successfully",
                        transferService.getAll()));
    }

    @GetMapping("/api/admin/transfers/status/{status}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get transfers by status")
    public ResponseEntity<ApiResponse<List<TransferResponse>>> getByStatus(
            @PathVariable TransferStatus status) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfers retrieved successfully",
                        transferService.getByStatus(status)));
    }

    @PatchMapping("/api/admin/transfers/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Approve a blocked transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Transfer approved successfully",
                        transferService.approveTransfer(
                                id, resolveUserId(userDetails))));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}