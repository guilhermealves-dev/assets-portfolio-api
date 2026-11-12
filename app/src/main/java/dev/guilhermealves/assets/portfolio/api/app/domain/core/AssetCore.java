package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.AssetMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.AssetRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetResponse;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetCore {

    private final AssetFireBaseAdapter assetFireBaseAdapter;
    private final AssetMapper assetMapper;

    public AssetResponse create(AssetRequest asset) throws Exception {
        AssetDocument docRequest = assetMapper.map(asset);
        AssetDocument doc = assetFireBaseAdapter.create(docRequest);
        return assetMapper.map(doc);
    }

    public AssetResponse update(AssetRequest asset) throws Exception {
        assetFireBaseAdapter.findById(asset.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ativo", asset.getId()));

        AssetDocument docRequest = assetMapper.map(asset);
        AssetDocument doc = assetFireBaseAdapter.update(docRequest);
        return assetMapper.map(doc);
    }

    public List<AssetResponse> findAll() throws Exception {
        List<AssetDocument> assetDocs = assetFireBaseAdapter.findAll();
        return assetMapper.mapList(assetDocs);
    }

    public Optional<AssetResponse> findById(String id) throws Exception {
        Optional<AssetDocument> optionalDoc = assetFireBaseAdapter.findById(id);
        if (optionalDoc.isPresent()) {
            return Optional.of(assetMapper.map(optionalDoc.get()));
        }
        return Optional.empty();
    }

    public void deleteById(String id) throws Exception {
        assetFireBaseAdapter.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ativo", id));

        assetFireBaseAdapter.deleteById(id);
    }
}
