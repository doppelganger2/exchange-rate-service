package com.example.exchangerateservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Currency conversion response for a single currency pair")
public record ConversionResponse(
    @Schema(description = "Provider metadata", requiredMode = Schema.RequiredMode.REQUIRED)
    ProviderInfo providerInfo,

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
        description = "Original amount in source currency",
        example = "100.00",
        minimum = "0",
        exclusiveMinimum = true,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    BigDecimal amount,

    @Schema(
        description = "Converted amount in target currency",
        example = "92.00",
        minimum = "0",
        exclusiveMinimum = true,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    BigDecimal result
) {}
