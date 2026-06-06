package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateCountryRequest;
import com.okanetransfer.dto.request.UpdateCountryRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.CountryLookupDto;
import com.okanetransfer.dto.response.CountryResponseDto;
import com.okanetransfer.dto.response.PaginationResponse;
import com.okanetransfer.service.CountryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @PostMapping("/admin/countries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryResponseDto>> createCountry(
            @Valid @RequestBody CreateCountryRequest request) {

        CountryResponseDto country = countryService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CountryResponseDto>builder()
                        .success(true)
                        .message("Country created successfully")
                        .data(country)
                        .build());
    }

    @PutMapping("/admin/countries/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryResponseDto>> updateCountry(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateCountryRequest request) {

        CountryResponseDto country = countryService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.<CountryResponseDto>builder()
                        .success(true)
                        .message("Country updated successfully")
                        .data(country)
                        .build()
        );
    }

    @PatchMapping("/admin/countries/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryResponseDto>> toggleActive(
            @PathVariable("id") Long id) {

        CountryResponseDto country = countryService.toggleActive(id);

        return ResponseEntity.ok(
                ApiResponse.<CountryResponseDto>builder()
                        .success(true)
                        .message("Country active status updated successfully")
                        .data(country)
                        .build()
        );
    }

    @PatchMapping("/admin/countries/{id}/toggle-sending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryResponseDto>> toggleSending(
            @PathVariable("id") Long id) {

        CountryResponseDto country = countryService.toggleSending(id);

        return ResponseEntity.ok(
                ApiResponse.<CountryResponseDto>builder()
                        .success(true)
                        .message("Country sending status updated successfully")
                        .data(country)
                        .build()
        );
    }

    @PatchMapping("/admin/countries/{id}/toggle-receiving")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CountryResponseDto>> toggleReceiving(
            @PathVariable("id") Long id) {

        CountryResponseDto country = countryService.toggleReceiving(id);

        return ResponseEntity.ok(
                ApiResponse.<CountryResponseDto>builder()
                        .success(true)
                        .message("Country receiving status updated successfully")
                        .data(country)
                        .build()
        );
    }

    @GetMapping("/admin/countries/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse<CountryResponseDto>>> searchCountries(

            @RequestParam(name="active" , required = false) Boolean active,
            @RequestParam(name="keyword" , required = false) String keyword,

            @RequestParam(name="page" , defaultValue = "0") int page,
            @RequestParam(name="size" , defaultValue = "10") int size,

            @RequestParam(name="sortBy" , defaultValue = "id") String sortBy,
            @RequestParam(name="direction" , defaultValue = "asc") String direction) {

        PaginationResponse<CountryResponseDto> result =
                countryService.search(
                        active,
                        keyword,
                        page,
                        size,
                        sortBy,
                        direction);

        return ResponseEntity.ok(
                ApiResponse.<PaginationResponse<CountryResponseDto>>builder()
                        .success(true)
                        .message("Countries retrieved successfully")
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/countries/lookup")
    public ResponseEntity<List<CountryLookupDto>> lookup(
            @RequestParam("keyword") String keyword) {

        return ResponseEntity.ok(countryService.searchLookup(keyword));
    }
}