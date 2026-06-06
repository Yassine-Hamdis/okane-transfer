package com.okanetransfer.controller;

import com.okanetransfer.dto.request.KycReviewRequest;
import com.okanetransfer.dto.response.ApiResponse;
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
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "KYC", description = "KYC/AML compliance management")
public class KycController {

    @Autowired private KycService     kycService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/flagged")
    @Operation(summary = "Get all flagged KYC records")
    public ResponseEntity<ApiResponse<List<KycRecordResponse>>> getFlagged() {
        return ResponseEntity.ok(
                ApiResponse.success("Flagged records retrieved successfully",
                        kycService.getFlagged()));
    }

    @GetMapping("/blocked")
    @Operation(summary = "Get all blocked KYC records")
    public ResponseEntity<ApiResponse<List<KycRecordResponse>>> getBlocked() {
        return ResponseEntity.ok(
                ApiResponse.success("Blocked records retrieved successfully",
                        kycService.getBlocked()));
    }

    @GetMapping("/watchlist-hits")
    @Operation(summary = "Get all OFAC watchlist hits")
    public ResponseEntity<ApiResponse<List<KycRecordResponse>>> getWatchlistHits() {
        return ResponseEntity.ok(
                ApiResponse.success("Watchlist hits retrieved successfully",
                        kycService.getWatchlistHits()));
    }

    @GetMapping("/suspicion")
    @Operation(summary = "Get all transfers with suspicion declared")
    public ResponseEntity<ApiResponse<List<KycRecordResponse>>> getSuspicion() {
        return ResponseEntity.ok(
                ApiResponse.success("Suspicion records retrieved successfully",
                        kycService.getSuspicionDeclared()));
    }

    @GetMapping("/transfer/{transferId}")
    @Operation(summary = "Get KYC record for a specific transfer")
    public ResponseEntity<ApiResponse<KycRecordResponse>> getByTransfer(
            @PathVariable("transferId") Long transferId) {
        return ResponseEntity.ok(
                ApiResponse.success("KYC record retrieved successfully",
                        kycService.getByTransfer(transferId)));
    }

    @PatchMapping("/{id}/review")
    @Operation(summary = "Manually review a KYC record")
    public ResponseEntity<ApiResponse<KycRecordResponse>> review(
            @PathVariable("id") Long id,
            @Valid @RequestBody KycReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("KYC record reviewed successfully",
                        kycService.manualReview(id, request,
                                resolveUserId(userDetails))));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}