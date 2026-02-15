package com.example.exchangerateservice.provider.exchangeratehost.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRateHostResponse(
    boolean success,
    String source,
    Map<String, BigDecimal> quotes,
    Long timestamp
) {}
