package com.example.exchangerateservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Bulk currency conversion response from a single source currency to multiple target currencies")
public record MultiConversionResponse(
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
        description = "Original amount in source currency",
        example = "100.00",
        minimum = "0",
        exclusiveMinimum = true,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    BigDecimal amount,

    @Schema(description = "Map of target currency codes to their converted amounts",
            example = "{\"EUR\": 92.00, \"GBP\": 79.00, \"JPY\": 14950.00}",
            requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, BigDecimal> results
) {}
