package com.example.exchangerateservice.dto.response;

import java.math.BigDecimal;

public record RateResponse(
    String from,
    String to,
    BigDecimal rate
) {}
