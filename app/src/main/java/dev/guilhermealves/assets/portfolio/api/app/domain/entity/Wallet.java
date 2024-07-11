package dev.guilhermealves.assets.portfolio.api.app.domain.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Wallet {

    private String id;
    private String name;
    private User user;
    private String icon;
    private String color;
}
