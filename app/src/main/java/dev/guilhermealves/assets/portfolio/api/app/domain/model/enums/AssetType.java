package dev.guilhermealves.assets.portfolio.api.app.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AssetType {
    CRYPTO,
    STOCK_US,
    STOCK_BR,
    ETF,
    FII,
    COMMODITY,
    CASH,
    OTHER;

    @JsonCreator
    public static AssetType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (AssetType t : values()) {
            if (t.name().equalsIgnoreCase(value.trim())) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de ativo inválido: " + value);
    }
}
