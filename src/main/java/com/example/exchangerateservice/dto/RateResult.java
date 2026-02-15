package com.example.exchangerateservice.dto;

import java.math.BigDecimal;

public record RateResult(ExchangeRateData data, BigDecimal rate) {}
