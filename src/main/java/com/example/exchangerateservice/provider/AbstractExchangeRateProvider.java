package com.example.exchangerateservice.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for exchange rate providers.
 * Provides common functionality for parsing rates and currencies.
 */
public abstract class AbstractExchangeRateProvider implements ExchangeRateProvider {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String getName() {
        return type().getDisplayName();
    }

    /**
     * Parses raw currency rates from string keys to Currency instances.
     * Skips non-ISO-4217 currency codes with a warning.
     *
     * @param rawRates map of currency code strings to exchange rates
     * @return map of Currency instances to exchange rates
     */
    protected Map<Currency, BigDecimal> parseRates(Map<String, BigDecimal> rawRates) {
        Map<Currency, BigDecimal> parsedRates = new HashMap<>();

        for (Map.Entry<String, BigDecimal> entry : rawRates.entrySet()) {
            Currency currency = parseCurrencySafely(entry.getKey());
            if (currency != null) {
                parsedRates.put(currency, entry.getValue());
            }
        }

        return Map.copyOf(parsedRates);
    }

    /**
     * Safely parses a currency code string to a Currency instance.
     * Logs a warning and returns null for invalid codes.
     *
     * @param currencyCode the currency code to parse
     * @return Currency instance or null if invalid
     */
    protected Currency parseCurrencySafely(String currencyCode) {
        try {
            return Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException _) {
            log.warn("Skipping non-ISO-4217 currency: {}", currencyCode);
            return null;
        }
    }
}
