package com.example.exchangerateservice.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeRateProviderTypeTest {

    @Test
    void fromIdReturnsMatchingType() {
        assertEquals(ExchangeRateProviderType.EXCHANGERATE_HOST,
                ExchangeRateProviderType.fromId("exchangerate_host"));
    }

    @Test
    void fromIdRejectsUnknownProvider() {
        assertThrows(IllegalArgumentException.class,
                () -> ExchangeRateProviderType.fromId("missing"));
    }
}
