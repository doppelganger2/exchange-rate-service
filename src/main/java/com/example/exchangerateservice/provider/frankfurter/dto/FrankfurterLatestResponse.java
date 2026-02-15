package com.example.exchangerateservice.provider.frankfurter.dto;

import java.math.BigDecimal;
import java.util.Map;

public record FrankfurterLatestResponse(
    String base,
    String date,
    Map<String, BigDecimal> rates
) {}
