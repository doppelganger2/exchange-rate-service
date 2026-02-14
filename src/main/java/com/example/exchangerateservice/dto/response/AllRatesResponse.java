package com.example.exchangerateservice.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record AllRatesResponse(
    String base,
    Map<String, BigDecimal> rates
) {}
