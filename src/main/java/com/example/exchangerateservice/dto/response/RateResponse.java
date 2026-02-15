package com.example.exchangerateservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Exchange rate response for a single currency pair")
public record RateResponse(
    @Schema(
        description = "Source currency code (ISO 4217)",
        example = "USD",
        pattern = "^[A-Z]{3}$",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String from,

    @Schema(
        description = "Target currency code (ISO 4217)",
        example = "EUR",
        pattern = "^[A-Z]{3}$",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String to,

    @Schema(
        description = "Exchange rate from source to target currency",
        example = "0.92",
        minimum = "0",
        exclusiveMinimum = true,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    BigDecimal rate
) {}
