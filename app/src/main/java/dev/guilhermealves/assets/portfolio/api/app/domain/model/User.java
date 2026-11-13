package dev.guilhermealves.assets.portfolio.api.app.domain.model;

import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class User {

    private String id;
    private String name;
    private String email;
    private UserRole role;
}
