package com.example.exchangerateservice.provider;

import com.example.exchangerateservice.dto.ExchangeRateData;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProviderRegistryTest {

    @Test
    void getProviderReturnsMatchingInstance() {
        ExchangeRateProvider provider = new StubProvider(ExchangeRateProviderType.EXCHANGERATE_HOST);
        ProviderRegistry registry = new ProviderRegistry(List.of(provider));

        assertSame(provider, registry.getProvider(ExchangeRateProviderType.EXCHANGERATE_HOST));
    }

    @Test
    void getOrderedStartingWithMovesPreferredProviderToFront() {
        ExchangeRateProvider primary = new StubProvider(ExchangeRateProviderType.EXCHANGERATE_HOST);
        ExchangeRateProvider secondary = new StubProvider(ExchangeRateProviderType.FRANKFURTER);
        ProviderRegistry registry = new ProviderRegistry(List.of(primary, secondary));

        List<ExchangeRateProvider> ordered = registry.getOrderedStartingWith(ExchangeRateProviderType.FRANKFURTER);

        assertEquals(List.of(secondary, primary), ordered);
    }

    @Test
    void getAvailableTypesReturnsConfiguredOrder() {
        ExchangeRateProvider primary = new StubProvider(ExchangeRateProviderType.EXCHANGERATE_HOST);
        ExchangeRateProvider secondary = new StubProvider(ExchangeRateProviderType.FRANKFURTER);
        ProviderRegistry registry = new ProviderRegistry(List.of(primary, secondary));

        assertEquals(
                List.of(ExchangeRateProviderType.EXCHANGERATE_HOST, ExchangeRateProviderType.FRANKFURTER),
                registry.getAvailableTypes()
        );
    }

    @Test
    void getProviderThrowsForMissingProvider() {
        ExchangeRateProvider provider = new StubProvider(ExchangeRateProviderType.EXCHANGERATE_HOST);
        ProviderRegistry registry = new ProviderRegistry(List.of(provider));

        assertThrows(IllegalArgumentException.class,
                () -> registry.getProvider(ExchangeRateProviderType.FRANKFURTER));
    }

    private record StubProvider(ExchangeRateProviderType type) implements ExchangeRateProvider {

        @Override
            public ExchangeRateData getRates(Currency baseCurrency) {
                return new ExchangeRateData(baseCurrency, Map.of(), type, Instant.EPOCH);
            }

            @Override
            public String getName() {
                return type.getDisplayName();
            }
        }
}
