package dev.guilhermealves.assets.portfolio.api.app.domain.mapper;

import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Asset;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssetMapper {

    Asset mapper(AssetDocument document);

    AssetDocument mapper(Asset asset);

    List<Asset> mapperAssetList(List<AssetDocument> documents);

    List<AssetDocument> mapperDocList(List<Asset> assets);
}
