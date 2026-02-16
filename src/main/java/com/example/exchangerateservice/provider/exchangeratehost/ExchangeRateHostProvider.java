package com.example.exchangerateservice.provider.exchangeratehost;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.AbstractExchangeRateProvider;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
import com.example.exchangerateservice.provider.exchangeratehost.dto.ExchangeRateHostResponse;
import com.example.exchangerateservice.provider.util.TimestampParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(1)
@ConditionalOnProperty(prefix = "exchange-rate.providers.exchangerate-host", name = "enabled", havingValue = "true")
public class ExchangeRateHostProvider extends AbstractExchangeRateProvider {

    private final ExchangeRateHostClient client;
    private final String accessKey;

    public ExchangeRateHostProvider(
            ExchangeRateHostClient client,
            @Value("${exchange-rate.providers.exchangerate-host.access-key}") String accessKey) {
        this.client = client;
        this.accessKey = accessKey;
    }

    @Override
    public ExchangeRateData getRates(Currency baseCurrency) {
        ExchangeRateHostResponse response = client.getLatestRates(accessKey, baseCurrency.getCurrencyCode());

        if (!response.success()) {
            throw new ExchangeRateUnavailableException("exchangerate.host API call failed");
        }

        Map<Currency, BigDecimal> rates = parseRatesWithPrefix(response.source(), response.quotes());
        Instant providerTimestamp = TimestampParser.parseEpochSeconds(response.timestamp());
        return new ExchangeRateData(baseCurrency, rates, type(), providerTimestamp);
    }

    @Override
    public ExchangeRateProviderType type() {
        return ExchangeRateProviderType.EXCHANGERATE_HOST;
    }

    /**
     * Convert rate keys from strings to Currency instances.
     * Keys are in format "SOURCETARGET" (e.g., "UAHUSD", "UAHEUR").
     * Strips the source currency prefix and parses the target currency.
     * Non-ISO-4217 codes (e.g., "BTC", "XAU") are skipped with a warning.
     */
    private Map<Currency, BigDecimal> parseRatesWithPrefix(String sourceCurrency, Map<String, BigDecimal> quotes) {
        Map<Currency, BigDecimal> parsedRates = new HashMap<>();
        int prefixLength = sourceCurrency.length();

        for (Map.Entry<String, BigDecimal> entry : quotes.entrySet()) {
            String key = entry.getKey();

            if (key.length() <= prefixLength || !key.startsWith(sourceCurrency)) {
                log.warn("Unexpected quote key format: {}", key);
                continue;
            }

            String targetCurrencyCode = key.substring(prefixLength);
            Currency targetCurrency = parseCurrencySafely(targetCurrencyCode);
            if (targetCurrency != null) {
                parsedRates.put(targetCurrency, entry.getValue());
            }
        }
        return Map.copyOf(parsedRates);
    }

}
