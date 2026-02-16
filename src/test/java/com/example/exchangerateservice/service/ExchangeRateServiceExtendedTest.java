package com.example.exchangerateservice.service;

import com.example.exchangerateservice.dto.ConversionResult;
import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.dto.MultiConversionResult;
import com.example.exchangerateservice.dto.RateResult;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.ExchangeRateProvider;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
import com.example.exchangerateservice.provider.ProviderRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExchangeRateServiceExtendedTest {

    private StubProvider primary;
    private StubProvider secondary;
    private ExchangeRateService service;
    private Currency usd;
    private Currency eur;
    private Currency gbp;

    @BeforeEach
    void setUp() {
        primary = new StubProvider(ExchangeRateProviderType.EXCHANGERATE_HOST, true);
        secondary = new StubProvider(ExchangeRateProviderType.FRANKFURTER, false);
        ProviderRegistry registry = new ProviderRegistry(List.of(primary, secondary));
        service = new ExchangeRateService(registry, new StubCachedProviderService());
        usd = Currency.getInstance("USD");
        eur = Currency.getInstance("EUR");
        gbp = Currency.getInstance("GBP");
    }

    @Test
    void allProvidersFailPropagatesException() {
        secondary.setShouldFail(true);
        assertThrows(ExchangeRateUnavailableException.class, () -> service.getAllRates(usd, null, true));
    }

    @Test
    void specificProviderSuccessWithFallbackDoesNotTryNext() {
        primary.setShouldFail(false);
        ExchangeRateData data = service.getAllRates(usd, ExchangeRateProviderType.EXCHANGERATE_HOST, true);
        assertEquals(ExchangeRateProviderType.EXCHANGERATE_HOST, data.providerType());
        assertEquals(1, primary.calls.get());
        assertEquals(0, secondary.calls.get());
    }

    @Test
    void getRateReturnsCorrectValue() {
        primary.setShouldFail(false);
        RateResult result = service.getRate(usd, eur, ExchangeRateProviderType.EXCHANGERATE_HOST, false);
        assertEquals(BigDecimal.valueOf(0.9), result.rate());
        assertEquals(ExchangeRateProviderType.EXCHANGERATE_HOST, result.data().providerType());
    }

    @Test
    void getRateThrowsExceptionWhenCurrencyMissing() {
        primary.setShouldFail(false);
        assertThrows(IllegalArgumentException.class, () -> service.getRate(usd, gbp, ExchangeRateProviderType.EXCHANGERATE_HOST, false));
    }

    @Test
    void convertCalculatesCorrectAmount() {
        primary.setShouldFail(false);
        ConversionResult result = service.convert(usd, eur, BigDecimal.valueOf(100), ExchangeRateProviderType.EXCHANGERATE_HOST, false);
        // 100 * 0.9 = 90.00
        assertEquals(new BigDecimal("90.00"), result.result());
    }

    @Test
    void convertToMultipleCalculatesCorrectAmounts() {
        secondary.setShouldFail(false);
        MultiConversionResult result = service.convertToMultiple(usd, List.of(eur), BigDecimal.valueOf(100), ExchangeRateProviderType.FRANKFURTER, false);
        assertEquals(new BigDecimal("90.00"), result.results().get(eur));
        assertEquals(1, result.results().size());
    }

    @Test
    void convertToMultipleThrowsExceptionWhenAnyCurrencyMissing() {
        secondary.setShouldFail(false);
        assertThrows(IllegalArgumentException.class, () -> service.convertToMultiple(usd, List.of(eur, gbp), BigDecimal.valueOf(100), ExchangeRateProviderType.FRANKFURTER, false));
    }

    private static final class StubCachedProviderService extends CachedProviderService {
        @Override
        public ExchangeRateData getRates(ExchangeRateProvider provider, Currency baseCurrency) {
            return provider.getRates(baseCurrency);
        }
    }

    private static final class StubProvider implements ExchangeRateProvider {
        private final ExchangeRateProviderType type;
        private boolean shouldFail;
        private final AtomicInteger calls = new AtomicInteger();

        private StubProvider(ExchangeRateProviderType type, boolean shouldFail) {
            this.type = type;
            this.shouldFail = shouldFail;
        }

        public void setShouldFail(boolean shouldFail) {
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
        public ExchangeRateProviderType type() { return type; }

        @Override
        public String getName() { return type.getDisplayName(); }
    }
}
