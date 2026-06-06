package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateCountryRequest;
import com.okanetransfer.dto.request.UpdateCountryRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.CountryResponse;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.CountryService;
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
@RequestMapping("/api/admin/countries")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Countries", description = "Country management")
public class CountryController {

    @Autowired private CountryService countryService;
    @Autowired private UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all countries")
    public ResponseEntity<ApiResponse<List<CountryResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Countries retrieved successfully",
                        countryService.getAll()));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active countries")
    public ResponseEntity<ApiResponse<List<CountryResponse>>> getActive() {
        return ResponseEntity.ok(
                ApiResponse.success("Active countries retrieved successfully",
                        countryService.getActive()));
    }

    @GetMapping("/sending")
    @Operation(summary = "Get countries allowed for sending")
    public ResponseEntity<ApiResponse<List<CountryResponse>>> getSending() {
        return ResponseEntity.ok(
                ApiResponse.success("Sending countries retrieved successfully",
                        countryService.getSendingCountries()));
    }

    @GetMapping("/receiving")
    @Operation(summary = "Get countries allowed for receiving")
    public ResponseEntity<ApiResponse<List<CountryResponse>>> getReceiving() {
        return ResponseEntity.ok(
                ApiResponse.success("Receiving countries retrieved successfully",
                        countryService.getReceivingCountries()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get country by ID")
    public ResponseEntity<ApiResponse<CountryResponse>> getById(
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Country retrieved successfully",
                        countryService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Create country")
    public ResponseEntity<ApiResponse<CountryResponse>> create(
            @Valid @RequestBody CreateCountryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Country created successfully",
                        countryService.create(request, resolveUserId(userDetails))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update country")
    public ResponseEntity<ApiResponse<CountryResponse>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateCountryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Country updated successfully",
                        countryService.update(id, request, resolveUserId(userDetails))));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Activate or deactivate country")
    public ResponseEntity<ApiResponse<CountryResponse>> toggle(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success("Country status toggled successfully",
                        countryService.toggleActive(id, resolveUserId(userDetails))));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}