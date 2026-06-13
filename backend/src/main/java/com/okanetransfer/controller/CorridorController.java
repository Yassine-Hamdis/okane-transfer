package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateCorridorRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.CorridorResponse;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.CorridorService;
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
@RequestMapping("/api/admin/corridors")
@Tag(name = "Corridors", description = "Transfer corridor management")
public class CorridorController {

    @Autowired private CorridorService corridorService;
    @Autowired private UserRepository  userRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AGENT')")
    @Operation(summary = "Get all corridors")
    public ResponseEntity<ApiResponse<List<CorridorResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Corridors retrieved successfully",
                        corridorService.getAll()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_AGENT')")
    @Operation(summary = "Get active corridors")
    public ResponseEntity<ApiResponse<List<CorridorResponse>>> getActive() {
        return ResponseEntity.ok(
                ApiResponse.success("Active corridors retrieved successfully",
                        corridorService.getActive()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get corridor by ID")
    public ResponseEntity<ApiResponse<CorridorResponse>> getById(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Corridor retrieved successfully",
                        corridorService.getById(id)));
    }

    @GetMapping("/source/{countryId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get corridors by source country")
    public ResponseEntity<ApiResponse<List<CorridorResponse>>> getBySource(
            @PathVariable("countryId") Long countryId) {
        return ResponseEntity.ok(
                ApiResponse.success("Corridors retrieved successfully",
                        corridorService.getBySourceCountry(countryId)));
    }

    @GetMapping("/destination/{countryId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get corridors by destination country")
    public ResponseEntity<ApiResponse<List<CorridorResponse>>> getByDestination(
            @PathVariable("countryId") Long countryId) {
        return ResponseEntity.ok(
                ApiResponse.success("Corridors retrieved successfully",
                        corridorService.getByDestinationCountry(countryId)));
    }

    @PostMapping
    @Operation(summary = "Create corridor")
    public ResponseEntity<ApiResponse<CorridorResponse>> create(
            @Valid @RequestBody CreateCorridorRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Corridor created successfully",
                        corridorService.create(request, resolveUserId(userDetails))));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Activate or deactivate corridor")
    public ResponseEntity<ApiResponse<CorridorResponse>> toggle(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Corridor status toggled successfully",
                        corridorService.toggleActive(id, resolveUserId(userDetails))));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}