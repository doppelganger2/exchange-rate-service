package com.example.exchangerateservice.service;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.ExchangeRateProvider;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
import com.example.exchangerateservice.provider.ProviderRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeRateServiceTest {

    @Test
    void specificProviderWithoutFallbackPropagatesFailure() {
        StubProvider primary = new StubProvider(ExchangeRateProviderType.EXCHANGERATE_HOST, true);
        StubProvider secondary = new StubProvider(ExchangeRateProviderType.FRANKFURTER, false);
        ExchangeRateService service = new ExchangeRateService(new ProviderRegistry(List.of(primary, secondary)), null);

        Currency usd = Currency.getInstance("USD");
        Executable call = () -> service.getAllRates(usd, ExchangeRateProviderType.EXCHANGERATE_HOST, false);

        assertThrows(ExchangeRateUnavailableException.class, call);

        assertEquals(1, primary.calls.get());
        assertEquals(0, secondary.calls.get());
    }

    @Test
    void specificProviderWithFallbackTriesNextProvider() {
        StubProvider primary = new StubProvider(ExchangeRateProviderType.EXCHANGERATE_HOST, true);
        StubProvider secondary = new StubProvider(ExchangeRateProviderType.FRANKFURTER, false);
        ExchangeRateService service = new ExchangeRateService(new ProviderRegistry(List.of(primary, secondary)), null);

        ExchangeRateData data = service.getAllRates(Currency.getInstance("USD"), ExchangeRateProviderType.EXCHANGERATE_HOST, true);

        assertEquals(ExchangeRateProviderType.FRANKFURTER, data.providerType());
        assertEquals(1, primary.calls.get());
        assertEquals(1, secondary.calls.get());
    }

    @Test
    void noProviderSpecifiedUsesFallbackChain() {
        StubProvider primary = new StubProvider(ExchangeRateProviderType.EXCHANGERATE_HOST, true);
        StubProvider secondary = new StubProvider(ExchangeRateProviderType.FRANKFURTER, false);
        ExchangeRateService service = new ExchangeRateService(new ProviderRegistry(List.of(primary, secondary)), null);

        ExchangeRateData data = service.getAllRates(Currency.getInstance("USD"), null, true);

        assertEquals(ExchangeRateProviderType.FRANKFURTER, data.providerType());
        assertEquals(1, primary.calls.get());
        assertEquals(1, secondary.calls.get());
    }

    private static final class StubProvider implements ExchangeRateProvider {

        private final ExchangeRateProviderType type;
        private final boolean shouldFail;
        private final AtomicInteger calls = new AtomicInteger();

        private StubProvider(ExchangeRateProviderType type, boolean shouldFail) {
            this.type = type;
            this.shouldFail = shouldFail;
        }

        @Override
        public ExchangeRateData getRates(Currency baseCurrency) {
            calls.incrementAndGet();
            if (shouldFail) {
                throw new ExchangeRateUnavailableException("provider failed");
            }
            return new ExchangeRateData(
                    baseCurrency,
                    Map.of(Currency.getInstance("EUR"), BigDecimal.valueOf(0.9)),
                    type,
                    Instant.EPOCH
            );
        }

        @Override
        public ExchangeRateProviderType type() {
            return type;
        }

        @Override
        public String getName() {
            return type.getDisplayName();
        }
    }
}
