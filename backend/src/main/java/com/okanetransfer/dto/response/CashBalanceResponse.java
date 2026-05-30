package com.okanetransfer.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CashBalanceResponse {
    private String        currencyCode;
    private String        currencySymbol;
    private BigDecimal    currentBalance;
    private LocalDateTime updatedAt;
}