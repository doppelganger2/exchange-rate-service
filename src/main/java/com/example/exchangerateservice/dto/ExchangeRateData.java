package com.example.exchangerateservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

@Schema(description = "Exchange rate data containing base currency and rates to other currencies")
public record ExchangeRateData(
    @Schema(description = "Base currency code", example = "USD")
    Currency baseCurrency,

    @Schema(description = "Map of target currencies to their exchange rates relative to the base currency")
    Map<Currency, BigDecimal> rates
) {}
