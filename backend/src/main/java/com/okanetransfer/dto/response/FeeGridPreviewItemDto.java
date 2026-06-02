package com.okanetransfer.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class FeeGridPreviewItemDto {

    private Long id;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    private BigDecimal feeFixedAmount;
    private BigDecimal feePercentage;

    private Integer agencySharePercent;
    private Integer centralSharePercent;
}
