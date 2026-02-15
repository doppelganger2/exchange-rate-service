package com.example.exchangerateservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "All exchange rates from a base currency to all available currencies")
public record AllRatesResponse(
    @Schema(description = "Provider metadata", requiredMode = Schema.RequiredMode.REQUIRED)
    ProviderInfo providerInfo,

    @Schema(
        description = "Base currency code (ISO 4217)",
        example = "USD",
        pattern = "^[A-Z]{3}$",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String base,

    @Schema(description = "Map of currency codes to their exchange rates relative to the base currency",
            example = "{\"EUR\": 0.92, \"GBP\": 0.79, \"JPY\": 149.50}",
            requiredMode = Schema.RequiredMode.REQUIRED)
    Map<String, BigDecimal> rates
) {}
