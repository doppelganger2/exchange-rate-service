package com.example.exchangerateservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.main.allow-bean-definition-overriding=true"
)
@AutoConfigureTestRestTemplate
class ExchangeRateControllerIntegrationTest {

    private static final WireMockServer wireMockServer =
            new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    @DynamicPropertySource
    static void registerWireMockProperties(DynamicPropertyRegistry registry) {
        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
        }
        registry.add("exchange-rate.providers.exchangerate-host.base-url",
                () -> "http://localhost:" + wireMockServer.port());
        registry.add("exchange-rate.providers.frankfurter.base-url",
                () -> "http://localhost:" + wireMockServer.port());
        registry.add("exchange-rate.providers.freecurrencyapi.base-url",
                () -> "http://localhost:" + wireMockServer.port());
        registry.add("exchange-rate.providers.exchangerate-host.access-key", () -> "test");
        registry.add("exchange-rate.providers.freecurrencyapi.access-key", () -> "test");
    }

    @TestConfiguration
    static class CacheTestConfig {
        @Bean
        @Primary
        CacheManager cacheManager() {
            var cacheManager = new CaffeineCacheManager("exchangeRates");
            cacheManager.setCaffeine(com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                    .recordStats()
                    .expireAfterWrite(1, TimeUnit.MINUTES)
                    .maximumSize(100));
            return cacheManager;
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CacheManager cacheManager;

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        var cache = caffeineCache();
        cache.invalidateAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/rates/USD/EUR?provider=unknown",
            "/rates/USD?provider=unknown",
            "/rates/convert?from=USD&to=EUR&amount=1&provider=unknown",
            "/rates/USD/XYZ?provider=erh",
            "/rates/XYZ?provider=erh",
            "/rates/convert?from=USD&to=EUR&provider=erh",
            "/rates/convert?to=EUR&amount=1&provider=erh",
            "/rates/convert?from=USD&amount=1&provider=erh",
            "/rates/convert/bulk?from=USD&to=&amount=10&provider=erh",
            "/rates/convert/bulk?from=USD&to=EUR,XYZ&amount=10&provider=erh",
            "/rates/convert/bulk?to=EUR,GBP&amount=10&provider=erh",
            "/rates/convert/bulk?from=USD&to=EUR,GBP&provider=erh"
    })
    void rejectsWithBadRequest(String path) {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl(path),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/rates/USD/CAD?provider=erh",
            "/rates/convert?from=USD&to=CAD&amount=1&provider=erh",
            "/rates/convert/bulk?from=USD&to=EUR,CAD&amount=10&provider=erh"
    })
    void missingRateRejectsWithBadRequest(String path) {
        stubExchangeRateHost();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl(path),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/rates/USD/EUR",
            "/rates/USD",
            "/rates/convert?from=USD&to=EUR&amount=1",
            "/rates/convert/bulk?from=USD&to=EUR,GBP&amount=10"
    })
    void allProvidersFailureReturnsServiceUnavailable(String path) {
        stubAllProvidersFailure();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl(path),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0", "abc"})
    void convertRejectsInvalidAmount(String amount) {
        stubExchangeRateHost();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl("/rates/convert?from=USD&to=EUR&amount=" + amount + "&provider=erh"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getAllRatesUsesCacheAndRecordsStats() {
        stubExchangeRateHost();

        ResponseEntity<String> first = restTemplate.getForEntity(
                baseUrl("/rates/USD?provider=erh"),
                String.class
        );
        ResponseEntity<String> second = restTemplate.getForEntity(
                baseUrl("/rates/USD?provider=erh"),
                String.class
        );

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);

        var cache = caffeineCache();
        assertThat(cache.stats().missCount()).isEqualTo(1);
        assertThat(cache.stats().hitCount()).isEqualTo(1);

        wireMockServer.verify(1, WireMock.getRequestedFor(urlPathEqualTo("/live")));
    }

    @Test
    void getRateReturnsRateFromProvider() throws Exception {
        stubExchangeRateHost();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl("/rates/USD/EUR?provider=erh"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("rate").decimalValue()).isEqualByComparingTo("0.92");
        assertThat(body.get("providerInfo").get("providerId").asText()).isEqualTo("erh");
    }

    @Test
    void getAllRatesFallsBackWhenPreferredProviderFails() throws Exception {
        stubExchangeRateHostFailure();
        stubFrankfurter();
        stubFreeCurrencyApi();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl("/rates/USD?provider=erh&fallback=true"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        String providerId = body.get("providerInfo").get("providerId").asText();
        assertThat(Stream.of("ff", "fca")).contains(providerId);
    }

    @Test
    void getAllRatesUsesPreferredProviderWhenFallbackEnabledAndProviderSucceeds() throws Exception {
        stubExchangeRateHost();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl("/rates/USD?provider=erh&fallback=true"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        String providerId = body.get("providerInfo").get("providerId").asText();
        assertThat(providerId).isEqualTo("erh");
    }

    @Test
    void getAllRatesDoesNotReturnCachedFallbackDataWhenFallbackIsDisabled() throws Exception {

        stubExchangeRateHostFailure();
        stubFrankfurter();

        ResponseEntity<String> firstResponse = restTemplate.getForEntity(
                baseUrl("/rates/USD?provider=erh&fallback=true"),
                String.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode firstBody = objectMapper.readTree(firstResponse.getBody());
        assertThat(firstBody.get("providerInfo").get("providerId").asText()).isEqualTo("ff");

        ResponseEntity<String> secondResponse = restTemplate.getForEntity(
                baseUrl("/rates/USD?provider=erh&fallback=false"),
                String.class
        );

        assertThat(secondResponse.getStatusCode())
                .as("Should return 503 Service Unavailable when preferred provider fails and fallback is disabled, even if a previous fallback request succeeded")
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void getAllRatesDoesNotFallbackWhenDisabled() {
        stubExchangeRateHostFailure();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl("/rates/USD?provider=erh&fallback=false"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void convertAcceptsLowercaseCurrencyCodes() throws Exception {
        stubExchangeRateHost();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl("/rates/convert?from=usd&to=eur&amount=1&provider=erh"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("from").asText()).isEqualTo("USD");
        assertThat(body.get("to").asText()).isEqualTo("EUR");
    }

    @Test
    void convertReturnsRoundedResult() throws Exception {
        stubExchangeRateHost();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl("/rates/convert?from=USD&to=EUR&amount=10.005&provider=erh"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("result").decimalValue()).isEqualByComparingTo("9.20");
    }

    @Test
    void bulkConvertReturnsResultsForMultipleTargets() throws Exception {
        stubExchangeRateHost();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl("/rates/convert/bulk?from=USD&to=EUR,GBP,JPY&amount=10&provider=erh"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        JsonNode results = body.get("results");
        assertThat(results.get("EUR").decimalValue()).isEqualByComparingTo("9.20");
        assertThat(results.get("GBP").decimalValue()).isEqualByComparingTo("7.90");
        assertThat(results.get("JPY").decimalValue()).isEqualByComparingTo("1495.00");
    }

    @Test
    void bulkConvertCollapsesDuplicateTargetsWithLastValue() throws Exception {
        stubExchangeRateHost();

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl("/rates/convert/bulk?from=USD&to=EUR,JPY,EUR,GBP&amount=10&provider=erh"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode results = objectMapper.readTree(response.getBody()).get("results");
        List<String> keys = new ArrayList<>();
        results.fieldNames().forEachRemaining(keys::add);
        assertThat(keys).containsExactlyInAnyOrder("EUR", "JPY", "GBP");
        assertThat(results.get("EUR").decimalValue()).isEqualByComparingTo("9.20");
    }



    @Test
    void listProvidersReturnsAllProviderIds() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl("/providers"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        List<String> ids = new ArrayList<>();
        body.forEach(node -> ids.add(node.get("id").asText()));

        assertThat(ids).contains("erh", "ff", "fca");
    }

    private void stubAllProvidersFailure() {
        stubExchangeRateHostFailure();
        stubFrankfurterFailure();
        stubFreeCurrencyApiFailure();
    }

    private void stubExchangeRateHost() {
        wireMockServer.stubFor(get(urlPathEqualTo("/live"))
                .withQueryParam("access_key", WireMock.equalTo("test"))
                .withQueryParam("source", WireMock.equalTo("USD"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.createObjectNode()
                                .put("success", true)
                                .put("timestamp", 1700000000)
                                .put("source", "USD")
                                .set("quotes", objectMapper.valueToTree(Map.of(
                                        "USDEUR", new BigDecimal("0.92"),
                                        "USDGBP", new BigDecimal("0.79"),
                                        "USDJPY", new BigDecimal("149.50")
                                )))
                                .toString()
                        )));
    }

    private void stubExchangeRateHostFailure() {
        wireMockServer.stubFor(get(urlPathEqualTo("/live"))
                .withQueryParam("access_key", WireMock.equalTo("test"))
                .withQueryParam("source", WireMock.equalTo("USD"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.createObjectNode()
                                .put("success", false)
                                .put("timestamp", 1700000000)
                                .put("source", "USD")
                                .set("quotes", objectMapper.valueToTree(Map.of()))
                                .toString()
                        )));
    }

    private void stubFrankfurter() {
        wireMockServer.stubFor(get(urlPathEqualTo("/latest"))
                .withQueryParam("base", WireMock.equalTo("USD"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.createObjectNode()
                                .put("base", "USD")
                                .put("date", "2026-02-14")
                                .set("rates", objectMapper.valueToTree(Map.of(
                                        "EUR", new BigDecimal("0.91"),
                                        "GBP", new BigDecimal("0.78"),
                                        "JPY", new BigDecimal("149.10")
                                )))
                                .toString()
                        )));
    }

    private void stubFreeCurrencyApi() {
        var metaNode = objectMapper.createObjectNode();
        metaNode.put("last_updated_at", "2026-02-14T10:00:00Z");

        var dataNode = objectMapper.createObjectNode();
        dataNode.put("EUR", new BigDecimal("0.90"));
        dataNode.put("GBP", new BigDecimal("0.77"));
        dataNode.put("JPY", new BigDecimal("149.00"));

        var bodyNode = objectMapper.createObjectNode();
        bodyNode.set("meta", metaNode);
        bodyNode.set("data", dataNode);

        wireMockServer.stubFor(get(urlPathEqualTo("/latest"))
                .withQueryParam("apikey", WireMock.equalTo("test"))
                .withQueryParam("base_currency", WireMock.equalTo("USD"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(bodyNode.toString())
                        ));
    }

    private void stubFrankfurterFailure() {
        wireMockServer.stubFor(get(urlPathEqualTo("/latest"))
                .withQueryParam("base", WireMock.equalTo("USD"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)));
    }

    private void stubFreeCurrencyApiFailure() {
        wireMockServer.stubFor(get(urlPathEqualTo("/latest"))
                .withQueryParam("apikey", WireMock.equalTo("test"))
                .withQueryParam("base_currency", WireMock.equalTo("USD"))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)));
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + "/api" + path;
    }

    @SuppressWarnings("unchecked")
    private Cache<Object, Object> caffeineCache() {
        var cache = cacheManager.getCache("exchangeRates");
        assertThat(cache).isNotNull();
        Object nativeCache = cache.getNativeCache();
        assertThat(nativeCache).isInstanceOf(Cache.class);
        return (Cache<Object, Object>) nativeCache;
    }
}
