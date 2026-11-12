package dev.guilhermealves.assets.portfolio.api.app.domain.mapper;

import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.AssetRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssetMapper {

    @Mappings({
            @Mapping(target = "type", expression = "java(request.getType() != null ? request.getType().name() : null)")
    })
    AssetDocument map(AssetRequest request);

    @Mappings({
            @Mapping(target = "type", expression = "java(parseAssetType(document.getType()))")
    })
    AssetResponse map(AssetDocument document);

    List<AssetResponse> mapList(List<AssetDocument> documents);

    default AssetType parseAssetType(String type) {
        return AssetType.fromString(type);
    }
}
