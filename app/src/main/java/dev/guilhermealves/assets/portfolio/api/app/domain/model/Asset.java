package dev.guilhermealves.assets.portfolio.api.app.domain.model;

import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Asset {

    private String id;
    private String symbol;
    private String name;
    private AssetType type;
    private String imgUrl;
}
