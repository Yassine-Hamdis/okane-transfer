package com.okanetransfer.mapper;

import com.okanetransfer.dto.response.ExchangeRateProviderResponseDto;
import com.okanetransfer.entity.ExchangeRateProvider;

public class ExchangeRateProviderMapper {

    private ExchangeRateProviderMapper() {
    }

    public static ExchangeRateProviderResponseDto toDto(
            ExchangeRateProvider provider) {

        return ExchangeRateProviderResponseDto.builder()
                .id(provider.getId())
                .name(provider.getName())
                .baseUrl(provider.getBaseUrl())
                .active(provider.isActive())
                .description(provider.getDescription())
                .build();
    }
}
