package com.example.exchangerateservice.mapper;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.dto.response.AllRatesResponse;
import com.example.exchangerateservice.dto.response.ConversionResponse;
import com.example.exchangerateservice.dto.response.MultiConversionResponse;
import com.example.exchangerateservice.dto.response.RateResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ExchangeRateResponseMapper {

    public RateResponse toRateResponse(Currency from, Currency to, BigDecimal rate) {
        return new RateResponse(from.getCurrencyCode(), to.getCurrencyCode(), rate);
    }

    public AllRatesResponse toAllRatesResponse(ExchangeRateData data) {
        Map<String, BigDecimal> stringRates = new LinkedHashMap<>();
        data.rates().forEach((currency, rate) -> stringRates.put(currency.getCurrencyCode(), rate));
        return new AllRatesResponse(data.baseCurrency().getCurrencyCode(), stringRates);
    }

    public ConversionResponse toConversionResponse(Currency from, Currency to, BigDecimal amount, BigDecimal result) {
        return new ConversionResponse(from.getCurrencyCode(), to.getCurrencyCode(), amount, result);
    }

    public MultiConversionResponse toMultiConversionResponse(Currency from, BigDecimal amount, Map<Currency, BigDecimal> results) {
        Map<String, BigDecimal> stringResults = new LinkedHashMap<>();
        results.forEach((currency, value) -> stringResults.put(currency.getCurrencyCode(), value));
        return new MultiConversionResponse(from.getCurrencyCode(), amount, stringResults);
    }
}
