package com.okanetransfer.controller;

import com.okanetransfer.dto.request.UpdateRateRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.ExchangeRateResponse;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.ExchangeRateService;
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
@RequestMapping("/api/admin/exchange-rates")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Exchange Rates", description = "Exchange rate management")
public class ExchangeRateController {

    @Autowired private ExchangeRateService exchangeRateService;
    @Autowired private UserRepository      userRepository;

    @GetMapping("/{corridorId}/current")
    @Operation(summary = "Get current exchange rate for a corridor")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> getCurrent(
            @PathVariable("corridorId") Long corridorId) {
        return ResponseEntity.ok(
                ApiResponse.success("Current rate retrieved successfully",
                        exchangeRateService.getCurrentRate(corridorId)));
    }

    @GetMapping("/{corridorId}/history")
    @Operation(summary = "Get exchange rate history for a corridor")
    public ResponseEntity<ApiResponse<List<ExchangeRateResponse>>> getHistory(
            @PathVariable("corridorId") Long corridorId) {
        return ResponseEntity.ok(
                ApiResponse.success("Rate history retrieved successfully",
                        exchangeRateService.getHistory(corridorId)));
    }

    @PostMapping("/{corridorId}")
    @Operation(summary = "Manually update exchange rate for a corridor")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> update(
            @PathVariable("corridorId") Long corridorId,
            @Valid @RequestBody UpdateRateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Exchange rate updated successfully",
                        exchangeRateService.updateManually(
                                corridorId, request, resolveUserId(userDetails))));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}