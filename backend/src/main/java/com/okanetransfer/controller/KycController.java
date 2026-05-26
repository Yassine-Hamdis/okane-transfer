package com.okanetransfer.controller;

import com.okanetransfer.dto.request.KycReviewRequest;
import com.okanetransfer.dto.response.KycRecordResponse;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.KycService;
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
@RequestMapping("/api/admin/kyc")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "KYC", description = "KYC/AML compliance management — Admin only")
public class KycController {

    @Autowired
    private KycService kycService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/flagged")
    @Operation(summary = "Get all flagged KYC records")
    public ResponseEntity<List<KycRecordResponse>> getFlagged() {
        return ResponseEntity.ok(kycService.getFlagged());
    }

    @GetMapping("/blocked")
    @Operation(summary = "Get all blocked KYC records")
    public ResponseEntity<List<KycRecordResponse>> getBlocked() {
        return ResponseEntity.ok(kycService.getBlocked());
    }

    @GetMapping("/watchlist-hits")
    @Operation(summary = "Get all OFAC watchlist hits")
    public ResponseEntity<List<KycRecordResponse>> getWatchlistHits() {
        return ResponseEntity.ok(kycService.getWatchlistHits());
    }

    @GetMapping("/suspicion")
    @Operation(summary = "Get all transfers with suspicion declared")
    public ResponseEntity<List<KycRecordResponse>> getSuspicionDeclared() {
        return ResponseEntity.ok(kycService.getSuspicionDeclared());
    }

    @GetMapping("/transfer/{transferId}")
    @Operation(summary = "Get KYC record for a specific transfer")
    public ResponseEntity<KycRecordResponse> getByTransfer(
            @PathVariable Long transferId) {
        return ResponseEntity.ok(kycService.getByTransfer(transferId));
    }

    @PatchMapping("/{id}/review")
    @Operation(summary = "Manually review a KYC record")
    public ResponseEntity<KycRecordResponse> manualReview(
            @PathVariable Long id,
            @Valid @RequestBody KycReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long adminId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        return ResponseEntity.ok(kycService.manualReview(id, request, adminId));
    }
}