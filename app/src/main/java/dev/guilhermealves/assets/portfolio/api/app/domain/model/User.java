package dev.guilhermealves.assets.portfolio.api.app.domain.model;

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
    private String password;
}
