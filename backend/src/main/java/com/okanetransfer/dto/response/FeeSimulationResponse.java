package com.okanetransfer.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class FeeSimulationResponse {

    // INPUT
    private BigDecimal testAmount;
    private String testCurrency;

    // FX
    private BigDecimal exchangeRateToSource;
    private BigDecimal exchangeRateToDestination;
    private BigDecimal amountInSourceCurrency;

    private String agencyCurrency;
    private String destinationCurrency;

    // FEES
    private BigDecimal feeFixed;
    private BigDecimal feePercentageValue;
    private BigDecimal totalFee;

    private BigDecimal agencyShare;
    private BigDecimal centralShare;

    // FINAL
    private BigDecimal totalPaidByClient;
    private BigDecimal netSourceAmount;
    private BigDecimal amountDestination;
}