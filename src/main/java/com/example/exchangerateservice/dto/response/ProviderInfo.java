package com.example.exchangerateservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Metadata about the data provider")
public record ProviderInfo(
    @Schema(description = "Provider identifier", example = "exchangerate_host", requiredMode = Schema.RequiredMode.REQUIRED)
    String providerId,

    @Schema(description = "Provider display name", example = "exchangerate.host", requiredMode = Schema.RequiredMode.REQUIRED)
    String providerName,

    @Schema(description = "Timestamp of the rate data from the provider", example = "2026-02-15T17:30:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    Instant timestamp
) {}
