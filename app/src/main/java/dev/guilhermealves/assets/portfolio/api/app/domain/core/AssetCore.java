package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.AssetMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Asset;
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

    public Asset create(AssetDocument assetDoc) throws Exception {
        try {
            AssetDocument doc = assetFireBaseAdapter.create(assetDoc);

            return assetMapper.mapper(doc);

        } catch (Throwable t){
            log.error("Error on create - {}", t.getMessage());
            throw t;
        }
    }

    public Asset update(AssetDocument assetDoc) throws Exception {
        try {
            AssetDocument doc = assetFireBaseAdapter.update(assetDoc);

            return assetMapper.mapper(doc);

        } catch (Throwable t){
            log.error("Error on update - {}", t.getMessage());
            throw t;
        }
    }

    public List<Asset> findAll() throws Exception {
        try {
            List<AssetDocument> assetDocs = assetFireBaseAdapter.findAll();

            return assetMapper.mapperAssetList(assetDocs);

        } catch (Throwable t){
            log.error("Error on find All - {}", t.getMessage());
            throw t;
        }
    }

    public Optional<Asset> findById(String id) throws Exception {
        try {
            Optional<AssetDocument> optionalDoc = assetFireBaseAdapter.findById(id);

            if(optionalDoc.isPresent()){
                return Optional.of(assetMapper.mapper(optionalDoc.get()));
            }

            return Optional.empty();

        } catch (Throwable t){
            log.error("Error on find By Id - {}", t.getMessage());
            throw t;
        }
    }

    public void deleteById(String id) throws Exception {
        try {
            assetFireBaseAdapter.deleteById(id);
        } catch (Throwable t){
            log.error("Error on delete By Id - {}", t.getMessage());
            throw t;
        }
    }
}
