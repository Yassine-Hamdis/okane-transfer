package com.okanetransfer.controller;

import com.okanetransfer.dto.request.AgencyRequest;
import com.okanetransfer.dto.request.AssignManagerRequest;
import com.okanetransfer.dto.response.AgencyResponse;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.service.AgencyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/agencies")
public class AgencyController {

    private final AgencyService agencyService;

    public AgencyController(AgencyService agencyService) { this.agencyService = agencyService; }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AgencyResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("OK", agencyService.getAllAgencies()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgencyResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("OK", agencyService.getAgencyById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AgencyResponse>> create(@Valid @RequestBody AgencyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Created", agencyService.createAgency(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AgencyResponse>> update(@PathVariable Long id,
                                                              @Valid @RequestBody AgencyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Updated", agencyService.updateAgency(id, request)));
    }

    @PatchMapping("/{id}/assign-manager")
    public ResponseEntity<ApiResponse<Object>> assignManager(@PathVariable Long id,
                                                             @Valid @RequestBody AssignManagerRequest request) {
        agencyService.assignManager(id, request);
        return ResponseEntity.ok(ApiResponse.success("Manager assigned", null));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<Object>> suspend(@PathVariable Long id) {
        agencyService.suspendAgency(id);
        return ResponseEntity.ok(ApiResponse.success("Suspended", null));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Object>> activate(@PathVariable Long id) {
        agencyService.activateAgency(id);
        return ResponseEntity.ok(ApiResponse.success("Activated", null));
    }

    @GetMapping("/country/{countryId}")
    public ResponseEntity<ApiResponse<List<AgencyResponse>>> byCountry(@PathVariable Long countryId) {
        return ResponseEntity.ok(ApiResponse.success("OK", agencyService.getAgenciesByCountry(countryId)));
    }
}