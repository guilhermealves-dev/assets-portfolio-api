package dev.guilhermealves.assets.portfolio.api.app.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TransactionType {
    BUY,
    SELL,
    TRANSFER_IN,
    TRANSFER_OUT,
    SWAP,
    REWARD,
    STAKING,
    DIVIDEND,
    AIRDROP;

    @JsonCreator
    public static TransactionType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (TransactionType t : values()) {
            if (t.name().equalsIgnoreCase(value.trim())) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de transação inválido: " + value);
    }
}
