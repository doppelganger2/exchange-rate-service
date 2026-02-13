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
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

@Component
public class ExchangeRateHostProvider implements ExchangeRateProvider {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateHostProvider.class);

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
        ExchangeRateHostResponse response = client.getLatestRates(accessKey, baseCurrency.getCurrencyCode());

        if (!response.success()) {
            throw new ExchangeRateUnavailableException("exchangerate.host API call failed");
        }

        Map<Currency, BigDecimal> rates = parseRates(response.source(), response.quotes());
        return new ExchangeRateData(baseCurrency, rates);
    }

    @Override
    public String getName() {
        return "exchangerate.host";
    }

    /**
     * Convert rate keys from strings to Currency instances.
     * Keys are in format "SOURCETARGET" (e.g., "UAHUSD", "UAHEUR").
     * Strips the source currency prefix and parses the target currency.
     * Non-ISO-4217 codes (e.g., "BTC", "XAU") are skipped with a warning.
     */
    private Map<Currency, BigDecimal> parseRates(String sourceCurrency, Map<String, BigDecimal> quotes) {
        Map<Currency, BigDecimal> parsedRates = new HashMap<>();
        int prefixLength = sourceCurrency.length();

        for (Map.Entry<String, BigDecimal> entry : quotes.entrySet()) {
            String key = entry.getKey();

            // Strip source currency prefix (e.g., "UAHUSD" -> "USD")
            if (key.length() <= prefixLength || !key.startsWith(sourceCurrency)) {
                log.warn("Unexpected quote key format: {}", key);
                continue;
            }

            String targetCurrencyCode = key.substring(prefixLength);

            try {
                Currency targetCurrency = Currency.getInstance(targetCurrencyCode);
                parsedRates.put(targetCurrency, entry.getValue());
            } catch (IllegalArgumentException e) {
                // Skip non-ISO-4217 currencies (e.g., BTC, XAU)
                log.warn("Skipping non-ISO-4217 currency: {}", targetCurrencyCode);
            }
        }
        return parsedRates;
    }

}
