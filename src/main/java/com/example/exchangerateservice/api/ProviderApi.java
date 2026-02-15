package com.example.exchangerateservice.api;

import com.example.exchangerateservice.dto.response.ProviderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Providers", description = "Available exchange rate data providers")
public interface ProviderApi {

    @Operation(
            summary = "List available exchange rate providers",
            description = "Returns a list of all registered exchange rate providers with their IDs and display names",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved provider list",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProviderResponse.class),
                                    examples = @ExampleObject(
                                            name = "Available providers",
                                            value = "[{\"id\": \"exchangerate_host\", \"name\": \"exchangerate.host\"}]"
                                    )
                            )
                    )
            }
    )
    List<ProviderResponse> listProviders();
}
