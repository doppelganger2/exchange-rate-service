package com.example.exchangerateservice.provider;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProviderRegistry {

    private final Map<ExchangeRateProviderType, ExchangeRateProvider> providersByType;
    private final List<ExchangeRateProvider> orderedProviders;

    public ProviderRegistry(List<ExchangeRateProvider> providers) {
        this.orderedProviders = List.copyOf(providers);
        this.providersByType = providers.stream()
                .collect(Collectors.toMap(ExchangeRateProvider::type, Function.identity()));
    }

    public ExchangeRateProvider getProvider(ExchangeRateProviderType type) {
        ExchangeRateProvider provider = providersByType.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("Provider not available: " + type.getId());
        }
        return provider;
    }

    public List<ExchangeRateProvider> getAll() {
        return orderedProviders;
    }

    public List<ExchangeRateProvider> getOrderedStartingWith(ExchangeRateProviderType type) {
        ExchangeRateProvider preferred = getProvider(type);
        List<ExchangeRateProvider> result = new ArrayList<>();
        result.add(preferred);
        for (ExchangeRateProvider provider : orderedProviders) {
            if (provider.type() != type) {
                result.add(provider);
            }
        }
        return result;
    }

    public List<ExchangeRateProviderType> getAvailableTypes() {
        return orderedProviders.stream().map(ExchangeRateProvider::type).toList();
    }
}
