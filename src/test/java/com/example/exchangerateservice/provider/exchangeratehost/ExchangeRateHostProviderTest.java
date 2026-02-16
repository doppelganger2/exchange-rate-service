package com.example.exchangerateservice.provider.exchangeratehost;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
import com.example.exchangerateservice.provider.exchangeratehost.dto.ExchangeRateHostResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExchangeRateHostProviderTest {

    @Test
    void getRatesMapsResponseAndSkipsInvalidCurrenciesAndKeys() {
        ExchangeRateHostClient client = mock(ExchangeRateHostClient.class);
        ExchangeRateHostProvider provider = new ExchangeRateHostProvider(client, "test-key");

        Map<String, BigDecimal> quotes = new HashMap<>();
        quotes.put("USDEUR", BigDecimal.valueOf(0.92));
        quotes.put("USDJPY", BigDecimal.valueOf(150.12));
        quotes.put("USDBTC", BigDecimal.valueOf(0.00002));
        quotes.put("EURUSD", BigDecimal.valueOf(1.12));
        quotes.put("USD", BigDecimal.ONE);

        ExchangeRateHostResponse response = new ExchangeRateHostResponse(
                true,
                "USD",
                quotes,
                1700000000L
        );

        when(client.getLatestRates("test-key", "USD")).thenReturn(response);

        ExchangeRateData data = provider.getRates(Currency.getInstance("USD"));

        assertEquals(ExchangeRateProviderType.EXCHANGERATE_HOST, data.providerType());
        assertEquals("exchangerate.host", provider.getName());
        assertEquals(Currency.getInstance("USD"), data.baseCurrency());
        assertEquals(BigDecimal.valueOf(0.92), data.rates().get(Currency.getInstance("EUR")));
        assertEquals(BigDecimal.valueOf(150.12), data.rates().get(Currency.getInstance("JPY")));
        assertEquals(2, data.rates().size());
        assertEquals(Instant.ofEpochSecond(1700000000L), data.providerTimestamp());
    }

    @Test
    void getRatesThrowsWhenResponseIsNotSuccessful() {
        ExchangeRateHostClient client = mock(ExchangeRateHostClient.class);
        ExchangeRateHostProvider provider = new ExchangeRateHostProvider(client, "test-key");

        ExchangeRateHostResponse response = new ExchangeRateHostResponse(
                false,
                "USD",
                Map.of("USDEUR", BigDecimal.valueOf(0.92)),
                1700000000L
        );

        when(client.getLatestRates("test-key", "USD")).thenReturn(response);

        var usd = Currency.getInstance("USD");
        assertThrows(ExchangeRateUnavailableException.class,
                () -> provider.getRates(usd));
    }
}
