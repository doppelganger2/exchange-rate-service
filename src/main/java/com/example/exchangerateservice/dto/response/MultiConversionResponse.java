package com.example.exchangerateservice.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record MultiConversionResponse(
    String from,
    BigDecimal amount,
    Map<String, BigDecimal> results
) {}
