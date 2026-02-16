package com.example.exchangerateservice.provider.freecurrencyapi;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.AbstractExchangeRateProvider;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
import com.example.exchangerateservice.provider.freecurrencyapi.dto.FreeCurrencyApiMeta;
import com.example.exchangerateservice.provider.freecurrencyapi.dto.FreeCurrencyApiResponse;
import com.example.exchangerateservice.provider.util.TimestampParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

@Component
@Order(2)
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${exchange-rate.providers.freecurrencyapi.access-key:}')")
public class FreeCurrencyApiProvider extends AbstractExchangeRateProvider {

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

    private Instant parseTimestamp(FreeCurrencyApiMeta meta) {
        if (meta == null || meta.lastUpdatedAt() == null) {
            return Instant.now();
        }
        return TimestampParser.parseTimestamp(meta.lastUpdatedAt());
    }
}
