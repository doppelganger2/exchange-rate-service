package com.example.exchangerateservice.service;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.ExchangeRateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private final List<ExchangeRateProvider> providers;
    private final ExchangeRateService self;

    public ExchangeRateService(List<ExchangeRateProvider> providers, @Lazy ExchangeRateService self) {
        this.providers = providers;
        this.self = self;
    }

    /**
     * Fetch all exchange rates for a given base currency.
     * Results are cached for 1 minute.
     * Tries each provider in order; if one fails, falls back to the next.
     */
    @Cacheable(value = "exchangeRates", key = "#baseCurrency.currencyCode")
    public ExchangeRateData getAllRates(Currency baseCurrency) {
        for (ExchangeRateProvider provider : providers) {
            try {
                ExchangeRateData data = provider.getRates(baseCurrency);
                log.info("Fetched rates for {} from provider '{}'", baseCurrency.getCurrencyCode(), provider.getName());
                return data;
            } catch (Exception e) {
                log.warn("Provider '{}' failed for base currency {}: {}", provider.getName(), baseCurrency.getCurrencyCode(), e.getMessage());
            }
        }
        throw new ExchangeRateUnavailableException("All exchange rate providers failed for base currency: " + baseCurrency.getCurrencyCode());
    }

    /**
     * Get the exchange rate from one currency to another.
     */
    public BigDecimal getRate(Currency from, Currency to) {
        ExchangeRateData data = self.getAllRates(from);
        BigDecimal rate = data.rates().get(to);
        if (rate == null) {
            throw new IllegalArgumentException("No rate available for " + from.getCurrencyCode() + " to " + to.getCurrencyCode());
        }
        return rate;
    }

    /**
     * Convert an amount from one currency to another.
     */
    public BigDecimal convert(Currency from, Currency to, BigDecimal amount) {
        BigDecimal rate = self.getRate(from, to);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Convert an amount from one currency to multiple target currencies.
     * Returns a map of target currency -> converted amount.
     */
    public Map<Currency, BigDecimal> convertToMultiple(Currency from, List<Currency> targets, BigDecimal amount) {
        ExchangeRateData data = self.getAllRates(from);
        Map<Currency, BigDecimal> results = new LinkedHashMap<>();
        for (Currency target : targets) {
            BigDecimal rate = data.rates().get(target);
            if (rate == null) {
                throw new IllegalArgumentException("No rate available for " + from.getCurrencyCode() + " to " + target.getCurrencyCode());
            }
            results.put(target, amount.multiply(rate).setScale(2, RoundingMode.HALF_UP));
        }
        return results;
    }
}
