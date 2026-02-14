package com.example.exchangerateservice.dto.response;

import java.math.BigDecimal;

public record ConversionResponse(
    String from,
    String to,
    BigDecimal amount,
    BigDecimal result
) {}
