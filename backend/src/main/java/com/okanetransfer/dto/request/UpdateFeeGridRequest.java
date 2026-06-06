package com.okanetransfer.dto.request;

import com.okanetransfer.entity.enums.TransferType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeeGridRequest {

    @NotNull
    private Long corridorId;

    @NotNull
    private Long currencyId;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal minAmount;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal maxAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal feeFixedAmount;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal feePercentage;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer agencySharePercent;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer centralSharePercent;

    @NotNull
    private TransferType transferType = TransferType.STANDARD;
}