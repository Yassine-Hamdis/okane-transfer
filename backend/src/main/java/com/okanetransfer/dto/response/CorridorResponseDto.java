package com.okanetransfer.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CorridorResponseDto {

    private Long id;

    // ================= COUNTRY =================
    private Long sourceCountryId;
    private String sourceCountryName;
    private String sourceCountryCode;

    private Long destinationCountryId;
    private String destinationCountryName;
    private String destinationCountryCode;

    // ================= CURRENCY =================
    private Long sourceCurrencyId;
    private String sourceCurrencyCode;
    private String sourceCurrencySymbol;

    private Long destinationCurrencyId;
    private String destinationCurrencyCode;
    private String destinationCurrencySymbol;

    // ================= STATUS =================
    private boolean active;

    // ================= AUDIT =================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}