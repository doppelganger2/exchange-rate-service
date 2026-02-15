package com.example.exchangerateservice.provider;

public enum ExchangeRateProviderType {
    EXCHANGERATE_HOST("exchangerate_host", "exchangerate.host"),
    FRANKFURTER("frankfurter", "Frankfurter"),
    FREECURRENCYAPI("freecurrencyapi", "FreecurrencyAPI");

    private final String id;
    private final String displayName;

    ExchangeRateProviderType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ExchangeRateProviderType fromId(String id) {
        for (ExchangeRateProviderType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown provider: " + id);
    }
}
