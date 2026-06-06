package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateFeeGridRequest;
import com.okanetransfer.dto.request.FeeSimulationRequest;
import com.okanetransfer.dto.request.UpdateFeeGridRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.FeeGridResponse;
import com.okanetransfer.dto.response.FeeSimulationResponse;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.FeeGridService;
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
@RequestMapping("/api")
@Tag(name = "Fee Grids", description = "Fee grid management and simulation")
public class FeeGridController {

    @Autowired private FeeGridService feeGridService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/admin/fee-grids")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all fee grids")
    public ResponseEntity<ApiResponse<List<FeeGridResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Fee grids retrieved successfully",
                        feeGridService.getAll()));
    }

    @GetMapping("/admin/fee-grids/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get fee grid by ID")
    public ResponseEntity<ApiResponse<FeeGridResponse>> getById(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Fee grid retrieved successfully",
                        feeGridService.getById(id)));
    }

    @GetMapping("/admin/fee-grids/corridor/{corridorId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get fee grids by corridor")
    public ResponseEntity<ApiResponse<List<FeeGridResponse>>> getByCorridor(
            @PathVariable("corridorId") Long corridorId) {
        return ResponseEntity.ok(
                ApiResponse.success("Fee grids retrieved successfully",
                        feeGridService.getByCorridor(corridorId)));
    }

    @PostMapping("/admin/fee-grids")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create fee grid")
    public ResponseEntity<ApiResponse<FeeGridResponse>> create(
            @Valid @RequestBody CreateFeeGridRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Fee grid created successfully",
                        feeGridService.create(request, resolveUserId(userDetails))));
    }

    @PutMapping("/admin/fee-grids/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update fee grid")
    public ResponseEntity<ApiResponse<FeeGridResponse>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateFeeGridRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Fee grid updated successfully",
                        feeGridService.update(id, request, resolveUserId(userDetails))));
    }

    @PatchMapping("/admin/fee-grids/{id}/toggle")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Activate or deactivate fee grid")
    public ResponseEntity<ApiResponse<FeeGridResponse>> toggle(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Fee grid status toggled successfully",
                        feeGridService.toggleActive(id, resolveUserId(userDetails))));
    }

    @PostMapping("/fees/simulate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_AGENT')")
    @Operation(summary = "Simulate fee before creating a transfer")
    public ResponseEntity<ApiResponse<FeeSimulationResponse>> simulate(
            @Valid @RequestBody FeeSimulationRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Fee simulation completed",
                        feeGridService.simulateFee(request)));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}