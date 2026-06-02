package com.okanetransfer.service;

import com.okanetransfer.entity.ExchangeRateProvider;

import java.math.BigDecimal;

public interface ExchangeRateProviderClient {

    BigDecimal getRate(String from, String to, ExchangeRateProvider provider);
}
