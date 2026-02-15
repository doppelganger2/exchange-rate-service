package com.example.exchangerateservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.examples.Example;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Configuration
public class OpenApiConfig {

    private static final String BASE_ERROR_RESPONSE_PATH = "#/components/schemas/ErrorResponse";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Exchange Rate Service")
                        .version("v1")
                        .description("The service serves as a middleware, fetching the data from a list of " +
                                "third-party providers, using the first one available "));
    }

    @Bean
    public OpenApiCustomizer globalResponsesCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
            pathItem.readOperations().forEach(operation -> {
                ApiResponse error400 = new ApiResponse()
                        .description("Bad Request - Invalid parameters or currency code")
                        .content(new Content().addMediaType(APPLICATION_JSON_VALUE,
                                buildErrorMediaType(400, "Bad Request", "Invalid parameters or currency code")));

                ApiResponse error500 = new ApiResponse()
                        .description("Internal Server Error - Unexpected error occurred")
                        .content(new Content().addMediaType(APPLICATION_JSON_VALUE,
                                buildErrorMediaType(500, "Internal Server Error", "Unexpected error occurred")));

                ApiResponse error503 = new ApiResponse()
                        .description("Service Unavailable - All exchange rate providers failed")
                        .content(new Content().addMediaType(APPLICATION_JSON_VALUE,
                                buildErrorMediaType(503, "Service Unavailable", "All exchange rate providers failed")));

                operation.getResponses().addApiResponse("400", error400);
                operation.getResponses().addApiResponse("500", error500);
                operation.getResponses().addApiResponse("503", error503);
            })
        );
    }

    private MediaType buildErrorMediaType(int status, String error, String message) {
        Map<String, Object> exampleValue = new LinkedHashMap<>();
        exampleValue.put("status", status);
        exampleValue.put("error", error);
        exampleValue.put("message", message);
        exampleValue.put("timestamp", "2024-01-15T10:30:00Z");

        Example example = new Example().value(exampleValue);

        return new MediaType()
                .schema(new Schema<>().$ref(BASE_ERROR_RESPONSE_PATH))
                .addExamples("example", example);
    }
}
