# Exchange Rate Service

A Spring Boot API that aggregates exchange rates from multiple public providers and exposes
rate and conversion endpoints with Swagger/OpenAPI docs. It includes provider fallback,
per-provider caching, and unit/integration tests.

## Features
- Fetch rates from multiple providers with an ordered fallback chain.
- Get a single rate, all rates for a base currency, or conversions (single and bulk).
- Cache provider responses for 1 minute (Caffeine) to minimize external calls.
- OpenAPI/Swagger UI for documentation and testing.

## Quick start (local)
Requirements:
- JDK 25 (see Gradle toolchain in `build.gradle`)

Run:
```bash
./gradlew bootRun
```

## Providers and API keys
The app supports three provider integrations. Frankfurter is always available, while the
other two are enabled automatically when their API keys are provided.

| Provider | ID | Notes |
| --- | --- | --- |
| Frankfurter | `ff` | Default provider. No API key required. |
| exchangerate.host | `erh` | Requires API key (`EXCHANGERATE_HOST_API_KEY`). |
| FreecurrencyAPI | `fca` | Requires API key (`FREECURRENCYAPI_KEY`). |

Provider ordering (used for fallback) is:
1) Frankfurter
2) exchangerate.host
3) FreecurrencyAPI

You can also list enabled providers at runtime:
- `GET /api/providers`

### Provider websites and API keys
- FreecurrencyAPI: https://freecurrencyapi.com/  
  Click "Create Free API Key" / "Get 100% Free API Key", create an account, then use the issued API key as `FREECURRENCYAPI_KEY`.
- exchangerate.host: https://exchangerate.host/  
  Click "Get Free API Key", sign up for a free plan, then use the API access key from your dashboard as `EXCHANGERATE_HOST_API_KEY`.
  
To use these providers, provide their API keys via environment variables, Gradle, or a local `.env` file:

Environment variables:
```
EXCHANGERATE_HOST_API_KEY=your_key_here
FREECURRENCYAPI_KEY=your_key_here
```

Gradle project properties:
```bash
./gradlew bootRun -PEXCHANGERATE_HOST_API_KEY=your_key_here -PFREECURRENCYAPI_KEY=your_key_here
```

Local `.env` file (project root; read by `bootRun`):
```
EXCHANGERATE_HOST_API_KEY=your_key_here
FREECURRENCYAPI_KEY=your_key_here
```

## Configuration
See `src/main/resources/application.yaml`.

The server defaults to:
- Port: `8081`
- Context path: `/api`

## API
Base URL: `http://localhost:8081/api`

Endpoints:
- `GET /rates/{from}/{to}`: single rate
- `GET /rates/{from}`: all rates for base currency
- `GET /rates/convert`: convert amount between two currencies
- `GET /rates/convert/bulk`: convert amount to multiple currencies

Optional query params for all endpoints:
- `provider`: provider ID (`ff`, `erh`, `fca`)
- `fallback`: `true` to allow fallback when a provider is specified

### Provider selection and fallback logic
- If `provider` is not specified, the service starts with Frankfurter and falls back to the other providers in order if needed.
- If `provider` is specified and `fallback=false` (default), the service will use only that provider and return `503` if it fails.
- If `provider` is specified and `fallback=true`, the service tries the specified provider first, then falls back to the others in order.

### Examples
```bash
# Single rate
curl "http://localhost:8081/api/rates/USD/EUR"

# All rates from a base currency
curl "http://localhost:8081/api/rates/USD"

# Convert amount
curl "http://localhost:8081/api/rates/convert?from=USD&to=EUR&amount=100"

# Bulk convert
curl "http://localhost:8081/api/rates/convert/bulk?from=USD&to=EUR,GBP,JPY&amount=100"

# Force a provider + allow fallback
curl "http://localhost:8081/api/rates/USD/EUR?provider=erh&fallback=true"
```

## Swagger / OpenAPI
- Swagger UI: `http://localhost:8081/api/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8081/api/v3/api-docs`

## Postman
Import the collection in `postman/exchange-rate-service.postman_collection.json`.
It includes all controller endpoints with variables for base URL, currencies, provider, and amount.

## How it works (high level)
- Controllers map HTTP requests to the service layer.
- `ExchangeRateService` resolves the provider (or fallback chain), fetches rates,
  and performs calculations.
- `CachedProviderService` applies a 1-minute per-provider cache for each base currency.
- Provider adapters map external API payloads into a common `ExchangeRateData` model.

## Docker
Build and run:
```bash
docker build -t exchange-rate-service .
docker run --rm -p 8081:8081 \
  -e EXCHANGERATE_HOST_API_KEY=your_key_here \
  -e FREECURRENCYAPI_KEY=your_key_here \
  exchange-rate-service
```

Or with Docker Compose (reads env vars from your shell or a `.env` file):
```bash
docker compose up --build
```

## Tests
```bash
./gradlew test
```

## Notes
- Currency codes use ISO 4217 (e.g., USD, EUR).
- Invalid or unsupported codes return `400 Bad Request`.
- If all providers fail, the API returns `503 Service Unavailable`.
