package com.example.exchangerateservice.provider.frankfurter;

import com.example.exchangerateservice.dto.ExchangeRateData;
import com.example.exchangerateservice.exception.ExchangeRateUnavailableException;
import com.example.exchangerateservice.provider.ExchangeRateProviderType;
import com.example.exchangerateservice.provider.frankfurter.dto.FrankfurterLatestResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FrankfurterProviderTest {

    @Test
    void getRatesMapsResponseAndSkipsInvalidCurrencies() {
        FrankfurterClient client = mock(FrankfurterClient.class);
        FrankfurterProvider provider = new FrankfurterProvider(client);

        Map<String, BigDecimal> rawRates = new HashMap<>();
        rawRates.put("EUR", BigDecimal.valueOf(0.92));
        rawRates.put("JPY", BigDecimal.valueOf(150.12));
        rawRates.put("BTC", BigDecimal.valueOf(0.00002));
        FrankfurterLatestResponse response = new FrankfurterLatestResponse("USD", "2026-02-15", rawRates);

        when(client.getLatestRates("USD")).thenReturn(response);

        ExchangeRateData data = provider.getRates(Currency.getInstance("USD"));

        assertEquals(ExchangeRateProviderType.FRANKFURTER, data.providerType());
        assertEquals("Frankfurter", provider.getName());
        assertEquals(Currency.getInstance("USD"), data.baseCurrency());
        assertEquals(BigDecimal.valueOf(0.92), data.rates().get(Currency.getInstance("EUR")));
        assertEquals(BigDecimal.valueOf(150.12), data.rates().get(Currency.getInstance("JPY")));
        assertEquals(2, data.rates().size());

        Instant expectedTimestamp = LocalDate.parse("2026-02-15").atStartOfDay(ZoneOffset.UTC).toInstant();
        assertEquals(expectedTimestamp, data.providerTimestamp());
    }

    @Test
    void getRatesThrowsWhenResponseIsNull() {
        FrankfurterClient client = mock(FrankfurterClient.class);
        FrankfurterProvider provider = new FrankfurterProvider(client);

        when(client.getLatestRates("USD")).thenReturn(null);
        var usd = Currency.getInstance("USD");
        assertThrows(ExchangeRateUnavailableException.class,
                () -> provider.getRates(usd));
    }

    @Test
    void getRatesThrowsWhenRatesMissing() {
        FrankfurterClient client = mock(FrankfurterClient.class);
        FrankfurterProvider provider = new FrankfurterProvider(client);

        when(client.getLatestRates("USD"))
                .thenReturn(new FrankfurterLatestResponse("USD", "2026-02-15", null));
        var usd = Currency.getInstance("USD");
        ExchangeRateUnavailableException ex = assertThrows(ExchangeRateUnavailableException.class,
                () -> provider.getRates(usd));
        assertNotNull(ex.getMessage());
    }
}
