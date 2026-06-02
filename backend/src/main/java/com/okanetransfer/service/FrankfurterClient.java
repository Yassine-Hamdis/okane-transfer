package com.okanetransfer.service;

import com.okanetransfer.dto.response.FrankfurterResponse;
import com.okanetransfer.entity.ExchangeRateProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service("FRANKFURTER")
@RequiredArgsConstructor
public class FrankfurterClient implements ExchangeRateProviderClient {

    private final RestTemplate restTemplate;

    @Override
    public BigDecimal getRate(String from, String to, ExchangeRateProvider provider) {

        String url = provider.getBaseUrl()
                + "?from=" + from
                + "&to=" + to;

        FrankfurterResponse response =
                restTemplate.getForObject(url, FrankfurterResponse.class);

        return response.getRates().get(to);
    }
}
