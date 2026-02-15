package com.example.exchangerateservice.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeRateProviderTypeTest {

    @Test
    void fromIdReturnsMatchingType() {
        assertEquals(ExchangeRateProviderType.EXCHANGERATE_HOST,
                ExchangeRateProviderType.fromId("exchangerate_host"));
        assertEquals(ExchangeRateProviderType.FRANKFURTER,
                ExchangeRateProviderType.fromId("frankfurter"));
        assertEquals(ExchangeRateProviderType.FREECURRENCYAPI,
                ExchangeRateProviderType.fromId("freecurrencyapi"));
    }

    @Test
    void fromIdRejectsUnknownProvider() {
        assertThrows(IllegalArgumentException.class,
                () -> ExchangeRateProviderType.fromId("missing"));
    }
}
