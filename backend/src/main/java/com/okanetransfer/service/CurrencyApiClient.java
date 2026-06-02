package com.okanetransfer.service;

import com.okanetransfer.dto.response.CurrencyApiResponse;
import com.okanetransfer.entity.ExchangeRateProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service("CURRENCY_API")
@RequiredArgsConstructor
public class CurrencyApiClient implements ExchangeRateProviderClient {

    private final RestTemplate restTemplate;

    @Override
    public BigDecimal getRate(String from, String to, ExchangeRateProvider provider) {

        String url = provider.getBaseUrl()
                + "?apikey=" + provider.getApiKey()
                + "&base_currency=" + from;

        CurrencyApiResponse response =
                restTemplate.getForObject(url, CurrencyApiResponse.class);

        return response.getData().get(to).getValue();
    }
}
