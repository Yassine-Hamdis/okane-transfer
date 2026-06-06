package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateCurrencyRequest;
import com.okanetransfer.dto.request.UpdateCurrencyRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.CurrencyResponse;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.CurrencyService;
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
@RequestMapping("/api/admin/currencies")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Currencies", description = "Currency management")
public class CurrencyController {

    @Autowired private CurrencyService currencyService;
    @Autowired private UserRepository  userRepository;

    @GetMapping
    @Operation(summary = "Get all currencies")
    public ResponseEntity<ApiResponse<List<CurrencyResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Currencies retrieved successfully",
                        currencyService.getAll()));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active currencies")
    public ResponseEntity<ApiResponse<List<CurrencyResponse>>> getActive() {
        return ResponseEntity.ok(
                ApiResponse.success("Active currencies retrieved successfully",
                        currencyService.getActive()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get currency by ID")
    public ResponseEntity<ApiResponse<CurrencyResponse>> getById(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Currency retrieved successfully",
                        currencyService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Create currency")
    public ResponseEntity<ApiResponse<CurrencyResponse>> create(
            @Valid @RequestBody CreateCurrencyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Currency created successfully",
                        currencyService.create(request, resolveUserId(userDetails))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update currency")
    public ResponseEntity<ApiResponse<CurrencyResponse>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateCurrencyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Currency updated successfully",
                        currencyService.update(id, request, resolveUserId(userDetails))));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Activate or deactivate currency")
    public ResponseEntity<ApiResponse<CurrencyResponse>> toggle(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Currency status toggled successfully",
                        currencyService.toggleActive(id, resolveUserId(userDetails))));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}