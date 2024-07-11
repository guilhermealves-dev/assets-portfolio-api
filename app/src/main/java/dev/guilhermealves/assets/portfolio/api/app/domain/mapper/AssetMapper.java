package dev.guilhermealves.assets.portfolio.api.app.domain.mapper;

import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AssetMapper {

    @Mappings({
            @Mapping(source = "img_url", target = "imgUrl")
    })
    Asset mapper(AssetDocument document);

    @Mappings({
            @Mapping(source = "imgUrl", target = "img_url")
    })
    AssetDocument mapper(Asset asset);
}
