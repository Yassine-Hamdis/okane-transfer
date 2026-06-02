package com.okanetransfer.mapper;

import com.okanetransfer.dto.response.ExchangeRateHistoryDto;
import com.okanetransfer.dto.response.ExchangeRateResponseDto;
import com.okanetransfer.entity.ExchangeRate;

public class ExchangeRateMapper {

    public static ExchangeRateResponseDto toDto(ExchangeRate rate) {

        return ExchangeRateResponseDto.builder()
                .id(rate.getId())
                .corridorId(rate.getCorridor().getId())
                .sourceCurrency(rate.getCorridor().getSourceCurrency().getCode())
                .destinationCurrency(rate.getCorridor().getDestinationCurrency().getCode())
                .rate(rate.getRate())
                .providerName(rate.getProvider().getName())
                .current(rate.isCurrent())
                .recordedAt(rate.getRecordedAt())
                .build();
    }

    public static ExchangeRateHistoryDto toHistoryDto(ExchangeRate rate) {

        return ExchangeRateHistoryDto.builder()
                .rate(rate.getRate())
                .providerName(rate.getProvider().getName())
                .current(rate.isCurrent())
                .recordedAt(rate.getRecordedAt())
                .build();
    }
}
