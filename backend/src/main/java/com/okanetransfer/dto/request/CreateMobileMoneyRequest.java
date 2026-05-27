package com.okanetransfer.dto.request;

import com.okanetransfer.entity.enums.MobileOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMobileMoneyRequest {

    @NotNull
    private Long transferId;

    @NotNull
    private MobileOperator operator;

    @NotBlank
    @Size(max = 20)
    private String walletPhone;
}