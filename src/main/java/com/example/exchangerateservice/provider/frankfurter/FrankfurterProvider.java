package com.example.exchangerateservice.provider.frankfurter;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.ExchangeRateProvider;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
import com.example.exchangerateservice.provider.frankfurter.dto.FrankfurterLatestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

@Component
public class FrankfurterProvider implements ExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(FrankfurterProvider.class);

    private final FrankfurterClient client;

    public FrankfurterProvider(FrankfurterClient client) {
        this.client = client;
    }

    @Override
    public ExchangeRateData getRates(Currency baseCurrency) {
        FrankfurterLatestResponse response = client.getLatestRates(baseCurrency.getCurrencyCode());
        if (response == null || response.rates() == null) {
            throw new ExchangeRateUnavailableException("Frankfurter API call failed");
        }

        Map<Currency, BigDecimal> rates = parseRates(response.rates());
        Instant providerTimestamp = parseDate(response.date());
        return new ExchangeRateData(baseCurrency, rates, type(), providerTimestamp);
    }

    @Override
    public ExchangeRateProviderType type() {
        return ExchangeRateProviderType.FRANKFURTER;
    }

    @Override
    public String getName() {
        return type().getDisplayName();
    }

    private Map<Currency, BigDecimal> parseRates(Map<String, BigDecimal> rawRates) {
        Map<Currency, BigDecimal> parsedRates = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : rawRates.entrySet()) {
            try {
                Currency currency = Currency.getInstance(entry.getKey());
                parsedRates.put(currency, entry.getValue());
            } catch (IllegalArgumentException e) {
                log.warn("Skipping non-ISO-4217 currency: {}", entry.getKey());
            }
        }
        return parsedRates;
    }

    private Instant parseDate(String date) {
        if (date == null || date.isBlank()) {
            return Instant.now();
        }
        return LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
