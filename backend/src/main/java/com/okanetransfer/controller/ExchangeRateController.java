package com.okanetransfer.controller;

import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.ExchangeRateHistoryDto;
import com.okanetransfer.dto.response.ExchangeRateResponseDto;
import com.okanetransfer.dto.response.PaginationResponse;
import com.okanetransfer.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping("/agent/exchange-rates/refresh/{corridorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<ExchangeRateResponseDto>> refreshRate(
            @PathVariable("corridorId") Long corridorId) {

        ExchangeRateResponseDto response =
                exchangeRateService.refresh(corridorId);

        return ResponseEntity.ok(
                ApiResponse.<ExchangeRateResponseDto>builder()
                        .success(true)
                        .message("Exchange rate refreshed successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/agent/exchange-rates/current/{corridorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<ExchangeRateResponseDto>> getCurrentRate(
            @PathVariable("corridorId") Long corridorId) {

        ExchangeRateResponseDto response =
                exchangeRateService.getCurrentRate(corridorId);

        return ResponseEntity.ok(
                ApiResponse.<ExchangeRateResponseDto>builder()
                        .success(true)
                        .message("Current exchange rate retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/admin/exchange-rates/history/{corridorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaginationResponse<ExchangeRateHistoryDto>>> getHistory(
            @PathVariable("corridorId") Long corridorId,
            @RequestParam(name="page" , defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "10") int size) {

        PaginationResponse<ExchangeRateHistoryDto> result =
                exchangeRateService.getHistory(corridorId, page, size);

        return ResponseEntity.ok(
                ApiResponse.<PaginationResponse<ExchangeRateHistoryDto>>builder()
                        .success(true)
                        .message("Exchange rate history retrieved successfully")
                        .data(result)
                        .build()
        );
    }
}
