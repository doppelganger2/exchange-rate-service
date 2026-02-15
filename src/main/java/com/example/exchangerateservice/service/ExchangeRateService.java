package com.example.exchangerateservice.service;

import com.example.exchangerateservice.dto.ConversionResult;
import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.dto.MultiConversionResult;
import com.example.exchangerateservice.dto.RateResult;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.ExchangeRateProvider;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
import com.example.exchangerateservice.provider.ProviderRegistry;
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

    private final ProviderRegistry registry;
    private final ExchangeRateService self;

    public ExchangeRateService(ProviderRegistry registry, @Lazy ExchangeRateService self) {
        this.registry = registry;
        this.self = self;
    }

    /**
     * Fetch all exchange rates for a given base currency.
     * Results are cached for 1 minute.
     */
    @Cacheable(value = "exchangeRates", key = "#baseCurrency.currencyCode + '-' + (#providerType != null ? #providerType.id : 'default')")
    public ExchangeRateData getAllRates(Currency baseCurrency, ExchangeRateProviderType providerType, boolean fallback) {
        if (providerType != null && !fallback) {
            ExchangeRateProvider provider = registry.getProvider(providerType);
            ExchangeRateData data = provider.getRates(baseCurrency);
            log.info("Fetched rates for {} from provider '{}'", baseCurrency.getCurrencyCode(), provider.getName());
            return data;
        }

        List<ExchangeRateProvider> providers = providerType != null
                ? registry.getOrderedStartingWith(providerType)
                : registry.getAll();

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

    public RateResult getRate(Currency from, Currency to, ExchangeRateProviderType providerType, boolean fallback) {
        ExchangeRateData data = self.getAllRates(from, providerType, fallback);
        BigDecimal rate = data.rates().get(to);
        if (rate == null) {
            throw new IllegalArgumentException("No rate available for " + from.getCurrencyCode() + " to " + to.getCurrencyCode());
        }
        return new RateResult(data, rate);
    }

    public ConversionResult convert(Currency from, Currency to, BigDecimal amount, ExchangeRateProviderType providerType, boolean fallback) {
        RateResult rateResult = getRate(from, to, providerType, fallback);
        BigDecimal result = amount.multiply(rateResult.rate()).setScale(2, RoundingMode.HALF_UP);
        return new ConversionResult(rateResult.data(), result);
    }

    public MultiConversionResult convertToMultiple(Currency from, List<Currency> targets, BigDecimal amount, ExchangeRateProviderType providerType, boolean fallback) {
        ExchangeRateData data = self.getAllRates(from, providerType, fallback);
        Map<Currency, BigDecimal> results = new LinkedHashMap<>();
        for (Currency target : targets) {
            BigDecimal rate = data.rates().get(target);
            if (rate == null) {
                throw new IllegalArgumentException("No rate available for " + from.getCurrencyCode() + " to " + target.getCurrencyCode());
            }
            results.put(target, amount.multiply(rate).setScale(2, RoundingMode.HALF_UP));
        }
        return new MultiConversionResult(data, results);
    }
}
