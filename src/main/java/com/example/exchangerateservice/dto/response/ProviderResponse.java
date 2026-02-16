package com.example.exchangerateservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Exchange rate provider information")
public record ProviderResponse(
    @Schema(description = "Provider identifier (use this value in the 'provider' query parameter)", example = "ff", requiredMode = Schema.RequiredMode.REQUIRED)
    String id,

    @Schema(description = "Provider display name", example = "Frankfurter", requiredMode = Schema.RequiredMode.REQUIRED)
    String name
) {}
