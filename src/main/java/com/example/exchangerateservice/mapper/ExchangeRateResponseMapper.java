package com.example.exchangerateservice.mapper;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.dto.response.AllRatesResponse;
import com.example.exchangerateservice.dto.response.ConversionResponse;
import com.example.exchangerateservice.dto.response.MultiConversionResponse;
import com.example.exchangerateservice.dto.response.ProviderInfo;
import com.example.exchangerateservice.dto.response.RateResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ExchangeRateResponseMapper {

    private ProviderInfo toProviderInfo(ExchangeRateData data) {
        return new ProviderInfo(
                data.providerType().getId(),
                data.providerType().getDisplayName(),
                data.providerTimestamp()
        );
    }

    public RateResponse toRateResponse(ExchangeRateData data, Currency from, Currency to, BigDecimal rate) {
        return new RateResponse(toProviderInfo(data), from.getCurrencyCode(), to.getCurrencyCode(), rate);
    }

    public AllRatesResponse toAllRatesResponse(ExchangeRateData data) {
        Map<String, BigDecimal> stringRates = new LinkedHashMap<>();
        data.rates().forEach((currency, rate) -> stringRates.put(currency.getCurrencyCode(), rate));
        return new AllRatesResponse(toProviderInfo(data), data.baseCurrency().getCurrencyCode(), stringRates);
    }

    public ConversionResponse toConversionResponse(ExchangeRateData data, Currency from, Currency to, BigDecimal amount, BigDecimal result) {
        return new ConversionResponse(toProviderInfo(data), from.getCurrencyCode(), to.getCurrencyCode(), amount, result);
    }

    public MultiConversionResponse toMultiConversionResponse(ExchangeRateData data, Currency from, BigDecimal amount, Map<Currency, BigDecimal> results) {
        Map<String, BigDecimal> stringResults = new LinkedHashMap<>();
        results.forEach((currency, value) -> stringResults.put(currency.getCurrencyCode(), value));
        return new MultiConversionResponse(toProviderInfo(data), from.getCurrencyCode(), amount, stringResults);
    }
}
