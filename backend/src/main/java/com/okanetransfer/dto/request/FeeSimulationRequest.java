package com.okanetransfer.dto.request;

import com.okanetransfer.entity.enums.TransferType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeeSimulationRequest {

    @NotNull
    private Long corridorId;

    @NotNull
    private Long currencyId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private TransferType transferType = TransferType.STANDARD;
}
