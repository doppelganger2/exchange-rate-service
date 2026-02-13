package com.example.exchangerateservice.provider.exchangeratehost;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.ExchangeRateProvider;
import com.example.exchangerateservice.provider.exchangeratehost.dto.ExchangeRateHostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

@Component
public class ExchangeRateHostProvider implements ExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateHostProvider.class);
    private static final Currency USD = Currency.getInstance("USD");

    private final ExchangeRateHostClient client;
    private final String accessKey;

    public ExchangeRateHostProvider(
            ExchangeRateHostClient client,
            @Value("${exchangerate.host.access-key}") String accessKey) {
        this.client = client;
        this.accessKey = accessKey;
    }

    @Override
    public ExchangeRateData getRates(Currency baseCurrency) {
        // Free tier only supports USD as base
        ExchangeRateHostResponse response = client.getLatestRates(accessKey, "USD");

        if (!response.success()) {
            throw new ExchangeRateUnavailableException("exchangerate.host API call failed");
        }

        Map<Currency, BigDecimal> usdRates = parseRates(response.rates());

        if (USD.equals(baseCurrency)) {
            return new ExchangeRateData(USD, usdRates);
        }

        // Cross-calculate: convert USD-based rates to requested base currency
        return crossCalculate(baseCurrency, usdRates);
    }

    @Override
    public String getName() {
        return "exchangerate.host";
    }

    /**
     * Convert rate keys from strings to Currency instances.
     * Keys are simple currency codes like "EUR", "GBP", etc.
     * Non-ISO-4217 codes (e.g., "BTC", "XAU") are skipped with a warning.
     */
    private Map<Currency, BigDecimal> parseRates(Map<String, BigDecimal> rates) {
        Map<Currency, BigDecimal> parsedRates = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : rates.entrySet()) {
            try {
                Currency targetCurrency = Currency.getInstance(entry.getKey());
                parsedRates.put(targetCurrency, entry.getValue());
            } catch (IllegalArgumentException e) {
                // Skip non-ISO-4217 currencies (e.g., BTC, XAU)
                log.warn("Skipping non-ISO-4217 currency: {}", entry.getKey());
            }
        }
        return parsedRates;
    }

    /**
     * Given USD-based rates, compute rates relative to a different base currency.
     * E.g., EUR/GBP = USD/GBP ÷ USD/EUR
     */
    private ExchangeRateData crossCalculate(Currency baseCurrency, Map<Currency, BigDecimal> usdRates) {
        BigDecimal baseInUsd = usdRates.get(baseCurrency);
        if (baseInUsd == null) {
            throw new IllegalArgumentException("Unknown currency: " + baseCurrency.getCurrencyCode());
        }

        Map<Currency, BigDecimal> convertedRates = new HashMap<>();
        for (Map.Entry<Currency, BigDecimal> entry : usdRates.entrySet()) {
            BigDecimal rate = entry.getValue().divide(baseInUsd, 6, RoundingMode.HALF_UP);
            convertedRates.put(entry.getKey(), rate);
        }
        return new ExchangeRateData(baseCurrency, convertedRates);
    }
}
