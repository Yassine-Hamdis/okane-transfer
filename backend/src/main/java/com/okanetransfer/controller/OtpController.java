package com.okanetransfer.controller;

import com.okanetransfer.dto.request.OtpVerifyRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.entity.User;
import com.okanetransfer.entity.enums.OtpType;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@Tag(name = "OTP", description = "One-time password generation and verification")
public class OtpController {

    @Autowired private OtpService     otpService;
    @Autowired private UserRepository userRepository;

    // ── 2FA endpoints ──────────────────────────────────────────────────────────

    @PostMapping("/generate/2fa")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate a 2FA OTP for the authenticated user",
            description = "Returns plain code — in production sent via SMS only.")
    public ResponseEntity<ApiResponse<Map<String, String>>> generate2Fa(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        String code = otpService.generateAndSave(user.getId(), OtpType.TWO_FACTOR);

        // In production, remove the code from the response and send via SMS
        return ResponseEntity.ok(
                ApiResponse.success("OTP generated successfully", Map.of("code", code))
        );
    }

    @PostMapping("/verify/2fa")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verify a 2FA OTP",
            description = "Submit the 6-digit code. Blocked after 5 wrong attempts.")
    public ResponseEntity<ApiResponse<Void>> verify2Fa(
            @Valid @RequestBody OtpVerifyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        boolean valid = otpService.verify(user.getId(), request.getCode(), OtpType.TWO_FACTOR);

        if (valid) {
            return ResponseEntity.ok(
                    ApiResponse.success("OTP verified successfully"));
        }
        return ResponseEntity.badRequest().body(
                ApiResponse.error("Invalid or expired OTP"));
    }

    // ── Withdrawal OTP endpoints ───────────────────────────────────────────────

    @PostMapping("/generate/withdrawal/{transferId}")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER','ADMIN')")
    @Operation(summary = "Generate a withdrawal OTP for a transfer",
            description = "Agent calls this before payout. Code is given to the recipient.")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateWithdrawal(
            @PathVariable("transferId") Long transferId) {

        String code = otpService.generateForTransfer(transferId);
        return ResponseEntity.ok(
                ApiResponse.success("Withdrawal OTP generated", Map.of(
                        "code", code,
                        "transferId", String.valueOf(transferId)
                ))
        );
    }

    @PostMapping("/verify/withdrawal/{transferId}")
    @PreAuthorize("hasAnyRole('AGENT','MANAGER','ADMIN')")
    @Operation(summary = "Verify a withdrawal OTP",
            description = "Agent verifies the code presented by the recipient before paying out.")
    public ResponseEntity<ApiResponse<Void>> verifyWithdrawal(
            @PathVariable("transferId") Long transferId,
            @Valid @RequestBody OtpVerifyRequest request) {

        boolean valid = otpService.verifyForTransfer(transferId, request.getCode());

        if (valid) {
            return ResponseEntity.ok(
                    ApiResponse.success("Withdrawal code verified"));
        }
        return ResponseEntity.badRequest().body(
                ApiResponse.error("Invalid or expired withdrawal code"));
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}