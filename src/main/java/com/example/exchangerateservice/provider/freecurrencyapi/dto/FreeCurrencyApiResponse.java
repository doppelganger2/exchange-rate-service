package com.example.exchangerateservice.provider.freecurrencyapi.dto;

import java.math.BigDecimal;
import java.util.Map;

public record FreeCurrencyApiResponse(
        FreeCurrencyApiMeta meta,
        Map<String, BigDecimal> data
) {}
