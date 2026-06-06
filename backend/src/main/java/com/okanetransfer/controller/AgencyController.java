package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateAgencyRequest;
import com.okanetransfer.dto.request.UpdateAgencyRequest;
import com.okanetransfer.dto.response.AgencyResponse;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.AgencyService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/admin/agencies")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Agency Management", description = "Admin agency management")
public class AgencyController {

    @Autowired private AgencyService  agencyService;
    @Autowired private UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all agencies")
    public ResponseEntity<ApiResponse<List<AgencyResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Agencies retrieved successfully",
                        agencyService.getAllAgencies()));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active agencies")
    public ResponseEntity<ApiResponse<List<AgencyResponse>>> getActive() {
        return ResponseEntity.ok(
                ApiResponse.success("Active agencies retrieved successfully",
                        agencyService.getActiveAgencies()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agency by ID")
    public ResponseEntity<ApiResponse<AgencyResponse>> getById(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Agency retrieved successfully",
                        agencyService.getAgencyById(id)));
    }

    @GetMapping("/country/{countryId}")
    @Operation(summary = "Get agencies by country")
    public ResponseEntity<ApiResponse<List<AgencyResponse>>> getByCountry(
            @PathVariable("countryId") Long countryId) {
        return ResponseEntity.ok(
                ApiResponse.success("Agencies retrieved successfully",
                        agencyService.getAgenciesByCountry(countryId)));
    }

    @PostMapping
    @Operation(summary = "Create a new agency")
    public ResponseEntity<ApiResponse<AgencyResponse>> create(
            @Valid @RequestBody CreateAgencyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Agency created successfully",
                        agencyService.createAgency(request, resolveUserId(userDetails))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update agency info")
    public ResponseEntity<ApiResponse<AgencyResponse>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateAgencyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Agency updated successfully",
                        agencyService.updateAgency(id, request, resolveUserId(userDetails))));
    }

    @PatchMapping("/{id}/assign-manager")
    @Operation(summary = "Assign a manager to an agency")
    public ResponseEntity<ApiResponse<AgencyResponse>> assignManager(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long managerId = body.get("managerId");
        if (managerId == null) {
            throw new IllegalArgumentException("managerId is required");
        }

        return ResponseEntity.ok(
                ApiResponse.success("Manager assigned successfully",
                        agencyService.assignManager(id, managerId,
                                resolveUserId(userDetails))));
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspend an agency")
    public ResponseEntity<ApiResponse<Void>> suspend(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        agencyService.suspendAgency(id, resolveUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Agency suspended successfully"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a suspended agency")
    public ResponseEntity<ApiResponse<Void>> activate(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        agencyService.activateAgency(id, resolveUserId(userDetails));
        return ResponseEntity.ok(ApiResponse.success("Agency activated successfully"));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}