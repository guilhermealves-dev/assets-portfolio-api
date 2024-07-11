package dev.guilhermealves.assets.portfolio.api.app.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Asset {

    private String id;
    private String name;
    private String type;
    private String imgUrl;
}
