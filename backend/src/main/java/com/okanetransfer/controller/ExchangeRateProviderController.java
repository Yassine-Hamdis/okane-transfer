package com.okanetransfer.controller;

import com.okanetransfer.dto.request.CreateExchangeRateProviderRequest;
import com.okanetransfer.dto.request.UpdateExchangeRateProviderRequest;
import com.okanetransfer.dto.response.ApiResponse;
import com.okanetransfer.dto.response.ExchangeRateHistoryDto;
import com.okanetransfer.dto.response.ExchangeRateProviderResponseDto;
import com.okanetransfer.dto.response.PaginationResponse;
import com.okanetransfer.service.ExchangeRateProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/exchange-rate-providers")
@RequiredArgsConstructor
public class ExchangeRateProviderController {

    private final ExchangeRateProviderService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExchangeRateProviderResponseDto> create(
            @Valid @RequestBody CreateExchangeRateProviderRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExchangeRateProviderResponseDto> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateExchangeRateProviderRequest request) {

        return ResponseEntity.ok(
                service.update(id, request)
        );
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExchangeRateProviderResponseDto> activate(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(
                service.activate(id)
        );
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExchangeRateProviderResponseDto>> findAll() {

        return ResponseEntity.ok(
                service.findAll()
        );
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExchangeRateProviderResponseDto> getActiveProvider() {

        return ResponseEntity.ok(
                service.getActiveProvider()
        );
    }


}
