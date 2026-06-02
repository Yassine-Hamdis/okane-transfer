package com.okanetransfer.service;

import com.okanetransfer.entity.ExchangeRateProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeRateClientRouter {

    private final List<ExchangeRateProviderClient> clients;

    public BigDecimal getRate(
            String from,
            String to,
            ExchangeRateProvider provider) {

        return clients.stream()
                .filter(c -> c.getClass()
                        .getAnnotation(Service.class)
                        .value()
                        .equals(provider.getName()))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("No provider client found"))
                .getRate(from, to, provider);
    }
}
