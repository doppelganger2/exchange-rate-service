package com.example.exchangerateservice.dto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

public record MultiConversionResult(ExchangeRateData data, Map<Currency, BigDecimal> results) {}
