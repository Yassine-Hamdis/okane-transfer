package com.okanetransfer.controller;

import com.okanetransfer.dto.request.OtpVerifyRequest;
import com.okanetransfer.dto.response.OtpVerifyResponse;
import com.okanetransfer.entity.enums.OtpType;
import com.okanetransfer.security.CustomUserDetails;
import com.okanetransfer.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@Tag(name = "OTP", description = "One-time password generation and verification for 2FA and withdrawals")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @Operation(summary = "Generate a 2FA OTP",
            description = "Generates a 6-digit OTP for the authenticated user. " +
                    "In production this is sent via SMS — returned in response for dev/testing only.")
    @ApiResponse(responseCode = "200", description = "OTP generated — plain code returned")
    @PostMapping("/generate/2fa")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> generate2Fa(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(otpService.generateForUser(principal.getId(), OtpType.TWO_FACTOR));
    }

    @Operation(summary = "Verify a 2FA OTP",
            description = "Submit the 6-digit code received via SMS. " +
                    "Blocked after 5 wrong attempts.")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    @PostMapping("/verify/2fa")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OtpVerifyResponse> verify2Fa(
            @Valid @RequestBody OtpVerifyRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        boolean valid = otpService.verifyForUser(principal.getId(), request.getCode(), OtpType.TWO_FACTOR);
        return valid
                ? ResponseEntity.ok(new OtpVerifyResponse(true, "OTP verified successfully"))
                : ResponseEntity.badRequest().body(new OtpVerifyResponse(false, "Invalid or expired OTP"));
    }

    @Operation(summary = "Generate a withdrawal OTP for a transfer",
            description = "Links an OTP to a specific transfer. " +
                    "Agent calls this to get the code to give to the sender, " +
                    "who then tells the recipient.")
    @PostMapping("/generate/withdrawal/{transferId}")
    @PreAuthorize("hasAnyRole('ROLE_AGENT','ROLE_ADMIN')")
    public ResponseEntity<String> generateWithdrawal(
            @Parameter(description = "Transfer ID") @PathVariable Long transferId) {
        return ResponseEntity.ok(otpService.generateForTransfer(transferId));
    }

    @Operation(summary = "Verify a withdrawal OTP",
            description = "Agent verifies the code presented by the recipient before paying out.")
    @PostMapping("/verify/withdrawal/{transferId}")
    @PreAuthorize("hasRole('ROLE_AGENT')")
    public ResponseEntity<OtpVerifyResponse> verifyWithdrawal(
            @Parameter(description = "Transfer ID") @PathVariable Long transferId,
            @Valid @RequestBody OtpVerifyRequest request) {

        boolean valid = otpService.verifyForTransfer(transferId, request.getCode());
        return valid
                ? ResponseEntity.ok(new OtpVerifyResponse(true, "Withdrawal code verified"))
                : ResponseEntity.badRequest().body(new OtpVerifyResponse(false, "Invalid or expired OTP"));
    }
}
