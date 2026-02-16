package com.example.exchangerateservice.provider.freecurrencyapi;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.ExchangeRateProvider;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
import com.example.exchangerateservice.provider.freecurrencyapi.dto.FreeCurrencyApiMeta;
import com.example.exchangerateservice.provider.freecurrencyapi.dto.FreeCurrencyApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "exchange-rate.providers.freecurrencyapi", name = "enabled", havingValue = "true")
public class FreeCurrencyApiProvider implements ExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(FreeCurrencyApiProvider.class);

    private final FreeCurrencyApiClient client;
    private final String apiKey;

    public FreeCurrencyApiProvider(
            FreeCurrencyApiClient client,
            @Value("${exchange-rate.providers.freecurrencyapi.access-key}") String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
    }

    @Override
    public ExchangeRateData getRates(Currency baseCurrency) {
        FreeCurrencyApiResponse response = client.getLatestRates(apiKey, baseCurrency.getCurrencyCode());
        if (response == null || response.data() == null) {
            throw new ExchangeRateUnavailableException("FreecurrencyAPI call failed");
        }

        Map<Currency, BigDecimal> rates = parseRates(response.data());
        Instant providerTimestamp = parseTimestamp(response.meta());
        return new ExchangeRateData(baseCurrency, rates, type(), providerTimestamp);
    }

    @Override
    public ExchangeRateProviderType type() {
        return ExchangeRateProviderType.FREECURRENCYAPI;
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

    private Instant parseTimestamp(FreeCurrencyApiMeta meta) {
        if (meta == null || meta.lastUpdatedAt() == null || meta.lastUpdatedAt().isBlank()) {
            return Instant.now();
        }

        String raw = meta.lastUpdatedAt().trim();
        try {
            return Instant.parse(raw);
        } catch (DateTimeParseException ignored) {
        }

        String normalized = raw.contains(" ") && !raw.contains("T")
                ? raw.replace(" ", "T")
                : raw;

        try {
            return OffsetDateTime.parse(normalized).toInstant();
        } catch (DateTimeParseException ignored) {
        }

        try {
            LocalDateTime localDateTime = LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return localDateTime.atZone(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException e) {
            log.warn("Unable to parse FreecurrencyAPI timestamp: {}", raw);
            return Instant.now();
        }
    }
}
