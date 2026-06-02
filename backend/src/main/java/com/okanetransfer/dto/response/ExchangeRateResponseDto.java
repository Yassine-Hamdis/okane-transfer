package com.okanetransfer.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateResponseDto {

    private Long id;

    private Long corridorId;

    private String sourceCurrency;

    private String destinationCurrency;

    private BigDecimal rate;

    private String providerName;

    private boolean current;

    private LocalDateTime recordedAt;
}
