package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.AssetCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Asset;
import dev.guilhermealves.assets.portfolio.api.app.ports.in.ControllerIntegration;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("assets")
public class AssetControllerAdapter implements ControllerIntegration<Asset, AssetDocument, String> {

    private final AssetCore core;

    @Override
    @PostMapping
    public ResponseEntity<Asset> create(@RequestBody @Valid AssetDocument asset) {
        try {
            return new ResponseEntity<>(core.create(asset), HttpStatus.CREATED);
        } catch (Throwable t) {
            log.error("Error on create");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Asset> find(@PathVariable String id) {
        try {
            Optional<Asset> optional = core.findById(id);

            if(optional.isPresent()){
                return new ResponseEntity<>(optional.get(), HttpStatus.OK);
            }

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } catch (Throwable t) {
            log.error("Error on find");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GetMapping
    public ResponseEntity<List<Asset>> list() {
        try {
            List<Asset> assets = core.findAll();
            return new ResponseEntity<>(assets, HttpStatus.OK);
        } catch (Throwable t) {
            log.error("Error on list");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<Asset> update(@PathVariable String id, @RequestBody @Valid AssetDocument asset) {
        try {
            asset.setId(id);
            return new ResponseEntity<>(core.update(asset), HttpStatus.ACCEPTED);

        } catch (Throwable t) {
            log.error("Error on update");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            core.deleteById(id);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Throwable t) {
            log.error("Error on delete");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
