package com.example.exchangerateservice.dto;

import java.math.BigDecimal;

public record ConversionResult(ExchangeRateData data, BigDecimal result) {}
