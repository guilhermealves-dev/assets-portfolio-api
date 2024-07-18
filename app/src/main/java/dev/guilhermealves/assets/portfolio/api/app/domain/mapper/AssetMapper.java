package dev.guilhermealves.assets.portfolio.api.app.domain.mapper;

import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.AssetRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssetMapper {

    AssetDocument map(AssetRequest request);

    AssetResponse map(AssetDocument document);

    List<AssetResponse> mapList(List<AssetDocument> documents);
}
