package com.okanetransfer.controller;

import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.CurrencyResponseDto;
import com.okanetransfer.dto.response.PaginationResponse;
import com.okanetransfer.entity.Currency;
import com.okanetransfer.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/currencies")
    public ResponseEntity<Currency> createCurrency(@RequestBody Currency currency) {
        Currency saved = currencyService.createCurrency(currency);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/currencies/{id}")
    public ResponseEntity<Currency> updateCurrency(
            @PathVariable Long id,
            @RequestBody Currency currency) {

        Currency updated = currencyService.updateCurrency(id, currency);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/admin/currencies/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CurrencyResponseDto>> toggleStatus(@PathVariable Long id) {

        CurrencyResponseDto response = currencyService.toggleActive(id);

        return ResponseEntity.ok(
                ApiResponse.<CurrencyResponseDto>builder()
                        .success(true)
                        .message("Currency status updated successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/agent/currencies/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<PaginationResponse<CurrencyResponseDto>>> searchCurrencies(

            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String keyword,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        PaginationResponse<CurrencyResponseDto> result =
                currencyService.search(
                        active,
                        keyword,
                        page,
                        size,
                        sortBy,
                        direction);

        return ResponseEntity.ok(
                ApiResponse.<PaginationResponse<CurrencyResponseDto>>builder()
                        .success(true)
                        .message("Currencies retrieved successfully")
                        .data(result)
                        .build()
        );
    }
}