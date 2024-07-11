package dev.guilhermealves.assets.portfolio.api.app.domain.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetDocument {

    private String id;
    private String name;
    private String type;
    private String img_url;
}
