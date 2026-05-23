package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateTransferRequest;
import com.okanetransfer.dto.request.PayoutRequest;
import com.okanetransfer.dto.response.TransferResponse;
import com.okanetransfer.dto.response.TransferSummaryResponse;
import com.okanetransfer.dto.response.TransferTrackResponse;
import com.okanetransfer.security.CustomUserDetails;
import com.okanetransfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Transfers", description = "Transfer creation, payout, cancellation and tracking")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    // ── Agent endpoints ────────────────────────────────────────────────────────

    @Operation(summary = "Create a new transfer",
            description = "Agent creates a transfer. Calculates fees, generates withdrawal code, records cash operation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transfer created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or daily limit exceeded"),
            @ApiResponse(responseCode = "403", description = "Forbidden — ROLE_AGENT required")
    })
    @PostMapping("/api/agent/transfers")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<TransferResponse> create(
            @Valid @RequestBody CreateTransferRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                transferService.createTransfer(request, principal.getId(), principal.getAgencyId()));
    }

    @Operation(summary = "Get transfer by ID")
    @ApiResponse(responseCode = "404", description = "Transfer not found")
    @GetMapping("/api/agent/transfers/{id}")
    @PreAuthorize("hasAnyRole('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    public ResponseEntity<TransferResponse> getById(
            @Parameter(description = "Transfer database ID") @PathVariable Long id) {
        return ResponseEntity.ok(transferService.getById(id));
    }

    @Operation(summary = "Get transfer by withdrawal code",
            description = "Look up a transfer using the 8-character code given to the sender.")
    @GetMapping("/api/agent/transfers/code/{code}")
    @PreAuthorize("hasAnyRole('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    public ResponseEntity<TransferResponse> getByCode(
            @Parameter(description = "8-character withdrawal code e.g. ABC12345") @PathVariable String code) {
        return ResponseEntity.ok(transferService.getByWithdrawalCode(code));
    }

    @Operation(summary = "Pay out a transfer to recipient",
            description = "Agent enters the withdrawal code and recipient ID. Marks transfer as PAYE and deducts cash.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payout successful"),
            @ApiResponse(responseCode = "409", description = "Transfer already paid, cancelled or expired")
    })
    @PostMapping("/api/agent/transfers/payout")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<TransferResponse> payout(
            @Valid @RequestBody PayoutRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        return ResponseEntity.ok(
                transferService.processPayment(request, principal.getId(), principal.getAgencyId()));
    }

    @Operation(summary = "Cancel a transfer",
            description = "Only EN_ATTENTE transfers can be cancelled. Reverses the cash operation.")
    @PatchMapping("/api/agent/transfers/{id}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_AGENT','ROLE_MANAGER')")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails principal) {

        transferService.cancelTransfer(id, body.get("reason"), principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get agent's own transfers")
    @GetMapping("/api/agent/transfers/my")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<List<TransferSummaryResponse>> myTransfers(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(transferService.getByAgent(principal.getId()));
    }

    @Operation(summary = "Search transfers by recipient phone number")
    @GetMapping("/api/agent/transfers/search")
    @PreAuthorize("hasAnyRole('ROLE_AGENT','ROLE_MANAGER','ROLE_ADMIN')")
    public ResponseEntity<List<TransferSummaryResponse>> searchByPhone(
            @Parameter(description = "Recipient phone number (partial match)") @RequestParam String phone) {
        return ResponseEntity.ok(transferService.searchByRecipientPhone(phone));
    }

    // ── Client endpoints ───────────────────────────────────────────────────────

    @Operation(summary = "Get client's own transfers")
    @GetMapping("/api/client/transfers/my")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public ResponseEntity<List<TransferSummaryResponse>> clientTransfers(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(transferService.getByClient(principal.getId()));
    }

    @Operation(summary = "Track a transfer by withdrawal code",
            description = "Returns limited info — no sensitive sender/agent data. " +
                    "Use this for the client-facing tracking page.")
    @GetMapping("/api/client/transfers/tracking/{code}")
    @PreAuthorize("hasAnyRole('ROLE_CLIENT','ROLE_AGENT','ROLE_ADMIN')")
    public ResponseEntity<TransferTrackResponse> track(
            @Parameter(description = "8-character withdrawal code") @PathVariable String code) {
        return ResponseEntity.ok(transferService.trackByCode(code));
    }

    // ── Admin endpoints ────────────────────────────────────────────────────────

    @Operation(summary = "Get all transfers — Admin only")
    @GetMapping("/api/admin/transfers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<TransferSummaryResponse>> allTransfers() {
        return ResponseEntity.ok(transferService.getAll());
    }

    @Operation(summary = "Approve a transfer requiring admin approval",
            description = "Clears the requiresAdminApproval flag on high-value transfers.")
    @PatchMapping("/api/admin/transfers/{id}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<TransferResponse> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(transferService.approveTransfer(id, principal.getId()));
    }
}
