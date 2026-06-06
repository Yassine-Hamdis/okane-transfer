package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.TransferType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeSimulationResponse {
    private Long feeGridId;

    private BigDecimal sentAmount;
    private String sentCurrency;

    private BigDecimal feeFixedAmount;
    private BigDecimal feePercentage;
    private BigDecimal feeAmount;

    private BigDecimal amountAfterFee;

    private BigDecimal exchangeRate;
    private BigDecimal receivedAmount;
    private String receivedCurrency;

    private BigDecimal agencyShare;
    private BigDecimal centralShare;

    private TransferType transferType;
}