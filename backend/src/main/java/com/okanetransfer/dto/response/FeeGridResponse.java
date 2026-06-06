package com.okanetransfer.dto.response;

import com.okanetransfer.entity.enums.TransferType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeGridResponse {
    private Long id;
    private Long corridorId;
    private String corridorLabel;
    private Long currencyId;
    private String currencyCode;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal feeFixedAmount;
    private BigDecimal feePercentage;
    private Integer agencySharePercent;
    private Integer centralSharePercent;
    private TransferType transferType;
    private boolean active;
}