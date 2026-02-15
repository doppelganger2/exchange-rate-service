package com.example.exchangerateservice.provider.freecurrencyapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FreeCurrencyApiMeta(
        @JsonProperty("last_updated_at") String lastUpdatedAt
) {}
