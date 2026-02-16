package com.example.exchangerateservice.provider.freecurrencyapi;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
import com.example.exchangerateservice.provider.freecurrencyapi.dto.FreeCurrencyApiMeta;
import com.example.exchangerateservice.provider.freecurrencyapi.dto.FreeCurrencyApiResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FreeCurrencyApiProviderTest {

    @Test
    void getRatesMapsResponseAndSkipsInvalidCurrencies() {
        FreeCurrencyApiClient client = mock(FreeCurrencyApiClient.class);
        FreeCurrencyApiProvider provider = new FreeCurrencyApiProvider(client, "test");

        Map<String, BigDecimal> rawRates = new HashMap<>();
        rawRates.put("EUR", BigDecimal.valueOf(0.91));
        rawRates.put("JPY", BigDecimal.valueOf(150.12));
        rawRates.put("BTC", BigDecimal.valueOf(0.00002));
        FreeCurrencyApiResponse response = new FreeCurrencyApiResponse(
                new FreeCurrencyApiMeta("2026-02-15T10:15:30Z"),
                rawRates
        );

        when(client.getLatestRates("test", "USD")).thenReturn(response);

        ExchangeRateData data = provider.getRates(Currency.getInstance("USD"));

        assertEquals(ExchangeRateProviderType.FREECURRENCYAPI, data.providerType());
        assertEquals("FreecurrencyAPI", provider.getName());
        assertEquals(Currency.getInstance("USD"), data.baseCurrency());
        assertEquals(BigDecimal.valueOf(0.91), data.rates().get(Currency.getInstance("EUR")));
        assertEquals(BigDecimal.valueOf(150.12), data.rates().get(Currency.getInstance("JPY")));
        assertEquals(2, data.rates().size());
        assertEquals(Instant.parse("2026-02-15T10:15:30Z"), data.providerTimestamp());
    }

    @Test
    void getRatesThrowsWhenResponseIsNull() {
        FreeCurrencyApiClient client = mock(FreeCurrencyApiClient.class);
        FreeCurrencyApiProvider provider = new FreeCurrencyApiProvider(client, "test");

        when(client.getLatestRates("test", "USD")).thenReturn(null);
        var usd = Currency.getInstance("USD");
        assertThrows(ExchangeRateUnavailableException.class,
                () -> provider.getRates(usd));
    }

    @Test
    void getRatesThrowsWhenDataMissing() {
        FreeCurrencyApiClient client = mock(FreeCurrencyApiClient.class);
        FreeCurrencyApiProvider provider = new FreeCurrencyApiProvider(client, "test");

        when(client.getLatestRates("test", "USD"))
                .thenReturn(new FreeCurrencyApiResponse(new FreeCurrencyApiMeta(null), null));
        var usd = Currency.getInstance("USD");
        ExchangeRateUnavailableException ex = assertThrows(ExchangeRateUnavailableException.class,
                () -> provider.getRates(usd));
        assertNotNull(ex.getMessage());
    }

    @Test
    void getRatesUsesNowWhenMetaMissing() {
        FreeCurrencyApiClient client = mock(FreeCurrencyApiClient.class);
        FreeCurrencyApiProvider provider = new FreeCurrencyApiProvider(client, "test");

        Map<String, BigDecimal> rawRates = new HashMap<>();
        rawRates.put("EUR", BigDecimal.valueOf(0.91));
        rawRates.put("JPY", BigDecimal.valueOf(150.12));
        FreeCurrencyApiResponse response = new FreeCurrencyApiResponse(null, rawRates);

        when(client.getLatestRates("test", "USD")).thenReturn(response);

        Instant before = Instant.now();
        ExchangeRateData data = provider.getRates(Currency.getInstance("USD"));
        Instant after = Instant.now();
        Instant lowerBound = before.minusSeconds(1);
        Instant upperBound = after.plusSeconds(1);

        assertTrue(!data.providerTimestamp().isBefore(lowerBound) && !data.providerTimestamp().isAfter(upperBound));
    }

    @Test
    void getRatesUsesNowWhenMetaTimestampMissing() {
        FreeCurrencyApiClient client = mock(FreeCurrencyApiClient.class);
        FreeCurrencyApiProvider provider = new FreeCurrencyApiProvider(client, "test");

        Map<String, BigDecimal> rawRates = new HashMap<>();
        rawRates.put("EUR", BigDecimal.valueOf(0.91));
        rawRates.put("JPY", BigDecimal.valueOf(150.12));
        FreeCurrencyApiResponse response = new FreeCurrencyApiResponse(new FreeCurrencyApiMeta(null), rawRates);

        when(client.getLatestRates("test", "USD")).thenReturn(response);

        Instant before = Instant.now();
        ExchangeRateData data = provider.getRates(Currency.getInstance("USD"));
        Instant after = Instant.now();
        Instant lowerBound = before.minusSeconds(1);
        Instant upperBound = after.plusSeconds(1);

        assertTrue(!data.providerTimestamp().isBefore(lowerBound) && !data.providerTimestamp().isAfter(upperBound));
    }
}
