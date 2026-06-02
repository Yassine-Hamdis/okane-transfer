package com.okanetransfer.mapper;

import com.okanetransfer.dto.response.CurrencyResponseDto;
import com.okanetransfer.entity.Currency;

public class CurrencyMapper {

    public static CurrencyResponseDto toDto(Currency currency) {

        return CurrencyResponseDto.builder()
                .id(currency.getId())
                .code(currency.getCode())
                .name(currency.getName())
                .symbol(currency.getSymbol())
                .active(currency.isActive())
                .build();
    }
}