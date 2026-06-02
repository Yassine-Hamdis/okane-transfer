package com.okanetransfer.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateHistoryDto {

    private BigDecimal rate;
    private String providerName;
    private boolean current;
    private LocalDateTime recordedAt;
}
