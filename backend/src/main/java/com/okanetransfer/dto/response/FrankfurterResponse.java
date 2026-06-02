package com.okanetransfer.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class FrankfurterResponse {

    private Map<String, BigDecimal> rates;
}
