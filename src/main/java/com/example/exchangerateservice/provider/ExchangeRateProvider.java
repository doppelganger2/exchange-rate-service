package com.example.exchangerateservice.provider;

import com.example.exchangerateservice.dto.ExchangeRateData;

import java.util.Currency;

public interface ExchangeRateProvider {
    ExchangeRateData getRates(Currency baseCurrency);
    ExchangeRateProviderType type();
    String getName();
}
