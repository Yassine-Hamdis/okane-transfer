package com.okanetransfer.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class CurrencyApiResponse {

    private Map<String, CurrencyApiRate> data;

    @Getter
    @Setter
    public static class CurrencyApiRate {
        private BigDecimal value;
    }
}
