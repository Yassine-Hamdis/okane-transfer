package com.okanetransfer.mapper;

import com.okanetransfer.dto.response.CountryResponseDto;
import com.okanetransfer.entity.Country;

public class CountryMapper {

    public static CountryResponseDto toDto(Country country) {

        return CountryResponseDto.builder()
                .id(country.getId())
                .name(country.getName())
                .code(country.getCode())
                .active(country.isActive())
                .allowsSending(country.isAllowsSending())
                .allowsReceiving(country.isAllowsReceiving())
                .defaultCurrencyId(country.getDefaultCurrency().getId())
                .defaultCurrencyCode(country.getDefaultCurrency().getCode())
                .defaultCurrencyName(country.getDefaultCurrency().getName())
                .build();
    }
}
