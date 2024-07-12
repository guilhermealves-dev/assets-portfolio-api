package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.TransactionCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.TransactionDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Transaction;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.Wallet;
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
@RequestMapping("transactions")
public class TransactionControllerAdapter implements ControllerIntegration<Transaction, TransactionDocument, String> {

    private final TransactionCore core;

    @Override
    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody @Valid TransactionDocument transaction) {
        try {
            return new ResponseEntity<>(core.create(transaction), HttpStatus.CREATED);
        } catch (Throwable t) {
            log.error("Error on create");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> find(@PathVariable String id) {
        try {
            Optional<Transaction> optional = core.findById(id);

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
    public ResponseEntity<List<Transaction>> list() {
        try {
            List<Transaction> transactions = core.findAll();
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        } catch (Throwable t) {
            log.error("Error on list");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable String id, @RequestBody @Valid TransactionDocument transaction) {
        try {
            transaction.setId(id);
            return new ResponseEntity<>(core.update(transaction), HttpStatus.ACCEPTED);

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
