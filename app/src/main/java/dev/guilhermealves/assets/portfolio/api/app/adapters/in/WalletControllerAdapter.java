package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.entity.Wallet;
import dev.guilhermealves.assets.portfolio.api.app.ports.in.ControllerIntegration;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("wallets")
public class WalletControllerAdapter implements ControllerIntegration<Wallet, String> {

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @Override
    @PostMapping
    public ResponseEntity<Wallet> create(@RequestBody Wallet wallet) {
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Wallet> find(@PathVariable String id) {
        throw new UnsupportedOperationException("Unimplemented method 'find'");
    }

    @Override
    @GetMapping
    public ResponseEntity<List<Wallet>> list() {
        throw new UnsupportedOperationException("Unimplemented method 'list'");
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<Wallet> update(@PathVariable String id, @RequestBody Wallet Wallet) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}
