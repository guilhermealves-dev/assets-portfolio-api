package dev.guilhermealves.assets.portfolio.api.app.domain.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDocument {

    String id;
    String symbol;
    String name;
    String type;
    String imgUrl;
}
