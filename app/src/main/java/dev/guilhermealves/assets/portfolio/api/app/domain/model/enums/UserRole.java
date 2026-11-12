package dev.guilhermealves.assets.portfolio.api.app.domain.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Perfil de autorização do usuário no sistema")
public enum UserRole {
    ADMIN,
    NORMAL;

    public static UserRole fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return NORMAL;
        }
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return NORMAL;
        }
    }
}
