package com.okanetransfer.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCorridorRequest {

    @NotNull
    private Long sourceCountryId;

    @NotNull
    private Long destinationCountryId;

    @NotNull
    private Long sourceCurrencyId;

    @NotNull
    private Long destinationCurrencyId;
}