package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.TransferType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentFeeGridDto {

    private Long feeGridId;

    private Long corridorId;

    private String sourceCountry;
    private String destinationCountry;

    private String sourceCurrency;
    private String destinationCurrency;

    private TransferType transferType;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    private BigDecimal feeFixedAmount;
    private BigDecimal feePercentage;

    private Integer agencySharePercent;
    private Integer centralSharePercent;
}
