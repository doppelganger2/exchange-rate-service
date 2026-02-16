package com.example.exchangerateservice.provider.frankfurter;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.AbstractExchangeRateProvider;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
import com.example.exchangerateservice.provider.util.TimestampParser;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;

@Component
@Order(0)
public class FrankfurterProvider extends AbstractExchangeRateProvider {

    private final FrankfurterClient client;

    public FrankfurterProvider(FrankfurterClient client) {
        this.client = client;
    }

    @Override
    public ExchangeRateData getRates(Currency baseCurrency) {
        var response = client.getLatestRates(baseCurrency.getCurrencyCode());
        if (response == null || response.rates() == null) {
            throw new ExchangeRateUnavailableException("Frankfurter API call failed");
        }

        Map<Currency, BigDecimal> rates = parseRates(response.rates());
        Instant providerTimestamp = TimestampParser.parseDate(response.date());
        return new ExchangeRateData(baseCurrency, rates, type(), providerTimestamp);
    }

    @Override
    public ExchangeRateProviderType type() {
        return ExchangeRateProviderType.FRANKFURTER;
    }

}
