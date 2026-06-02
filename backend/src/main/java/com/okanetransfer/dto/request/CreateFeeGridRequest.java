package com.okanetransfer.dto.request;

import com.okanetransfer.entity.enums.TransferType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateFeeGridRequest {

    private Long corridorId;
    private TransferType transferType;

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    private BigDecimal feeFixedAmount;
    private BigDecimal feePercentage;

    private Integer agencySharePercent;
    private Integer centralSharePercent;
}
