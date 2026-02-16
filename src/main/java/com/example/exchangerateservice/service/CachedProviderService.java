package com.example.exchangerateservice.service;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.provider.ExchangeRateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Currency;

/**
 * Service that provides caching for exchange rate providers.
 * Each provider's data is cached independently using its provider type ID as part of the cache key.
 * This prevents cache poisoning when fallback mechanisms are used.
 */
@Service
public class CachedProviderService {

    private static final Logger log = LoggerFactory.getLogger(CachedProviderService.class);

    @Cacheable(value = "exchangeRates", key = "#baseCurrency.currencyCode + '-' + #provider.type().id", unless = "#result == null")
    public ExchangeRateData getRates(ExchangeRateProvider provider, Currency baseCurrency) {
        ExchangeRateData data = provider.getRates(baseCurrency);
        log.info("Fetched rates for {} from provider '{}'", baseCurrency.getCurrencyCode(), provider.getName());
        return data;
    }
}
