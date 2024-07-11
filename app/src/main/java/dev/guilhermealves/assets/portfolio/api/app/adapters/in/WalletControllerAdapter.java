package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.adapters.out.WalletFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Wallet;
import dev.guilhermealves.assets.portfolio.api.app.ports.in.ControllerIntegration;

import dev.guilhermealves.assets.portfolio.api.app.ports.out.DataBaseIntegration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("wallets")
public class WalletControllerAdapter implements ControllerIntegration<Wallet, String> {

    private final WalletFireBaseAdapter fireBaseAdapter;

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @Override
    @PostMapping
    public ResponseEntity<Wallet> create(@RequestBody @Valid Wallet wallet) {
        try {
            throw new UnsupportedOperationException("Unimplemented method 'create'");
        } catch (Throwable t) {
            log.error("Error on create");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Wallet> find(@PathVariable String id) {
        try {
            throw new UnsupportedOperationException("Unimplemented method 'find'");
        } catch (Throwable t) {
            log.error("Error on find");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GetMapping
    public ResponseEntity<List<Wallet>> list() {
        try {
            List<Wallet> wallets = fireBaseAdapter.findAll();
            return new ResponseEntity<>(wallets, HttpStatus.OK);
        } catch (Throwable t) {
            log.error("Error on list");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<Wallet> update(@PathVariable String id, @RequestBody @Valid Wallet Wallet) {
        try {
            throw new UnsupportedOperationException("Unimplemented method 'update'");
        } catch (Throwable t) {
            log.error("Error on update");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            throw new UnsupportedOperationException("Unimplemented method 'delete'");
        } catch (Throwable t) {
            log.error("Error on delete");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
