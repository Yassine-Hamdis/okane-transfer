package com.okanetransfer.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class ExchangeRateApiResponse {

    @JsonProperty("conversion_rates")
    private Map<String, BigDecimal> conversionRates;
}
