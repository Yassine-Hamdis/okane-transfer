package com.okanetransfer.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRateRequest {

    @NotNull
    @DecimalMin(value = "0.000001", message = "Rate must be greater than zero")
    private BigDecimal rate;
}
