package com.example.exchangerateservice.api;

import com.example.exchangerateservice.dto.response.AllRatesResponse;
import com.example.exchangerateservice.dto.response.ConversionResponse;
import com.example.exchangerateservice.dto.response.MultiConversionResponse;
import com.example.exchangerateservice.dto.response.RateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Tag(name = "Exchange Rate API", description = "Endpoints for currency exchange rates and conversions")
@Validated
public interface ExchangeRateApi {

    @Operation(
            summary = "Get exchange rate between two currencies",
            description = "Retrieves the current exchange rate from one currency to another",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved exchange rate",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RateResponse.class),
                                    examples = @ExampleObject(
                                            name = "USD to EUR rate",
                                            value = "{\"providerInfo\": {\"providerId\": \"ff\", \"providerName\": \"Frankfurter\", \"timestamp\": \"2026-02-15T17:30:00Z\"}, \"from\": \"USD\", \"to\": \"EUR\", \"rate\": 0.92}"
                                    )
                            )
                    )
            }
    )
    RateResponse getRate(
            @Parameter(description = "Source currency code (ISO 4217, 3-letter code)", example = "USD", required = true)
            @PathVariable Currency from,

            @Parameter(description = "Target currency code (ISO 4217, 3-letter code)", example = "EUR", required = true)
            @PathVariable Currency to,

            @Parameter(description = "Provider ID (get available IDs from GET /api/providers). If omitted, uses fallback chain.", example = "ff")
            @RequestParam(required = false) String provider,

            @Parameter(description = "If true and a specific provider is requested, falls back to other providers on failure. Ignored when provider is not specified.", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean fallback
    );

    @Operation(
            summary = "Get all exchange rates from a base currency",
            description = "Retrieves exchange rates from a base currency to all available currencies",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved all exchange rates",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AllRatesResponse.class),
                                    examples = @ExampleObject(
                                            name = "All rates from USD",
                                            value = "{\"providerInfo\": {\"providerId\": \"ff\", \"providerName\": \"Frankfurter\", \"timestamp\": \"2026-02-15T17:30:00Z\"}, \"base\": \"USD\", \"rates\": {\"EUR\": 0.92, \"GBP\": 0.79, \"JPY\": 149.50}}"
                                    )
                            )
                    )
            }
    )
    AllRatesResponse getAllRates(
            @Parameter(description = "Base currency code (ISO 4217, 3-letter code)", example = "USD", required = true)
            @PathVariable Currency from,

            @Parameter(description = "Provider ID (get available IDs from GET /api/providers). If omitted, uses fallback chain.", example = "ff")
            @RequestParam(required = false) String provider,

            @Parameter(description = "If true and a specific provider is requested, falls back to other providers on failure. Ignored when provider is not specified.", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean fallback
    );

    @Operation(
            summary = "Convert amount between two currencies",
            description = "Converts a specific amount from one currency to another using current exchange rates",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully converted amount",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ConversionResponse.class),
                                    examples = @ExampleObject(
                                            name = "Convert 100 USD to EUR",
                                            value = "{\"providerInfo\": {\"providerId\": \"ff\", \"providerName\": \"Frankfurter\", \"timestamp\": \"2026-02-15T17:30:00Z\"}, \"from\": \"USD\", \"to\": \"EUR\", \"amount\": 100.00, \"result\": 92.00}"
                                    )
                            )
                    )
            }
    )
    ConversionResponse convert(
            @Parameter(description = "Source currency code (ISO 4217, 3-letter code)", example = "USD", required = true)
            @RequestParam Currency from,

            @Parameter(description = "Target currency code (ISO 4217, 3-letter code)", example = "EUR", required = true)
            @RequestParam Currency to,

            @Parameter(description = "Amount to convert (must be positive)", example = "100.00", required = true)
            @RequestParam @Positive(message = "Amount must not be negative or zero") BigDecimal amount,

            @Parameter(description = "Provider ID (get available IDs from GET /api/providers). If omitted, uses fallback chain.", example = "ff")
            @RequestParam(required = false) String provider,

            @Parameter(description = "If true and a specific provider is requested, falls back to other providers on failure. Ignored when provider is not specified.", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean fallback
    );

    @Operation(
            summary = "Bulk convert amount to multiple currencies",
            description = "Converts a specific amount from one currency to multiple target currencies using current exchange rates",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully converted amount to multiple currencies",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MultiConversionResponse.class),
                                    examples = @ExampleObject(
                                            name = "Convert 100 USD to multiple currencies",
                                            value = "{\"providerInfo\": {\"providerId\": \"ff\", \"providerName\": \"Frankfurter\", \"timestamp\": \"2026-02-15T17:30:00Z\"}, \"from\": \"USD\", \"amount\": 100.00, \"results\": {\"EUR\": 92.00, \"GBP\": 79.00, \"JPY\": 14950.00}}"
                                    )
                            )
                    )
            }
    )
    MultiConversionResponse convertBulk(
            @Parameter(description = "Source currency code (ISO 4217, 3-letter code)", example = "USD", required = true)
            @RequestParam Currency from,

            @Parameter(description = "List of target currency codes (ISO 4217, 3-letter codes, comma-separated)", example = "EUR,GBP,JPY", required = true)
            @RequestParam @NotEmpty(message = "Target currency list must not be empty") List<Currency> to,

            @Parameter(description = "Amount to convert (must be positive)", example = "100.00", required = true)
            @RequestParam @Positive(message = "Amount must not be negative or zero") BigDecimal amount,

            @Parameter(description = "Provider ID (get available IDs from GET /api/providers). If omitted, uses fallback chain.", example = "ff")
            @RequestParam(required = false) String provider,

            @Parameter(description = "If true and a specific provider is requested, falls back to other providers on failure. Ignored when provider is not specified.", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean fallback
    );
}
