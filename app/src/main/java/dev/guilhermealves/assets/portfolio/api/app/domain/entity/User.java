package dev.guilhermealves.assets.portfolio.api.app.domain.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private String id;
    private String name;
    private String email;
    private String password;
}
