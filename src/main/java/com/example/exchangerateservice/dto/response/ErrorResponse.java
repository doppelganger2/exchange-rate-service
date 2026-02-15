package com.example.exchangerateservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Error response for failed requests")
public record ErrorResponse(
    @Schema(
        description = "HTTP status code",
        minimum = "400",
        maximum = "599",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    int status,

    @Schema(
        description = "HTTP status reason phrase",
        example = "Bad Request",
        minLength = 1,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String error,

    @Schema(
        description = "Detailed error message",
        example = "Invalid currency code: XYZ",
        minLength = 1,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String message,

    @Schema(
        description = "Timestamp when the error occurred",
        example = "2024-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Instant timestamp
) {}
