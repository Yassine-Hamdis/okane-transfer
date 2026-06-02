package com.okanetransfer.service;

import com.okanetransfer.dto.response.ExchangeRateApiResponse;
import com.okanetransfer.entity.ExchangeRateProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service("EXCHANGERATE_API")
@RequiredArgsConstructor
public class ExchangeRateApiClient implements ExchangeRateProviderClient {

    private final RestTemplate restTemplate;

    @Override
    public BigDecimal getRate(String from, String to, ExchangeRateProvider provider) {

        String url = provider.getBaseUrl()
                + "/" + provider.getApiKey()
                + "/latest/" + from;

        ExchangeRateApiResponse response =
                restTemplate.getForObject(url, ExchangeRateApiResponse.class);

        return response.getConversionRates().get(to);
    }
}
