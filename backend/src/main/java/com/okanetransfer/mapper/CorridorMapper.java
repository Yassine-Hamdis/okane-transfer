package com.okanetransfer.mapper;

import com.okanetransfer.entity.Corridor;

public class CorridorMapper {

    public static CorridorResponseDto toDto(Corridor corridor) {

        return CorridorResponseDto.builder()

                // ===== ID =====
                .id(corridor.getId())

                // ===== SOURCE COUNTRY =====
                .sourceCountryId(corridor.getSourceCountry().getId())
                .sourceCountryName(corridor.getSourceCountry().getName())
                .sourceCountryCode(corridor.getSourceCountry().getCode())

                // ===== DEST COUNTRY =====
                .destinationCountryId(corridor.getDestinationCountry().getId())
                .destinationCountryName(corridor.getDestinationCountry().getName())
                .destinationCountryCode(corridor.getDestinationCountry().getCode())

                // ===== SOURCE CURRENCY =====
                .sourceCurrencyId(corridor.getSourceCurrency().getId())
                .sourceCurrencyCode(corridor.getSourceCurrency().getCode())
                .sourceCurrencySymbol(corridor.getSourceCurrency().getSymbol())

                // ===== DEST CURRENCY =====
                .destinationCurrencyId(corridor.getDestinationCurrency().getId())
                .destinationCurrencyCode(corridor.getDestinationCurrency().getCode())
                .destinationCurrencySymbol(corridor.getDestinationCurrency().getSymbol())

                // ===== STATUS =====
                .active(corridor.isActive())

                // ===== AUDIT =====
                .createdAt(corridor.getCreatedAt())
                .updatedAt(corridor.getUpdatedAt())

                .build();
    }
}
