package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.TransactionCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.TransactionRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.TransactionResponse;
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
public class TransactionControllerAdapter implements ControllerIntegration<TransactionResponse, TransactionRequest, String> {

    private final TransactionCore core;

    @Override
    @PostMapping
    public ResponseEntity<TransactionResponse> create(@RequestBody @Valid TransactionRequest transaction) {
        try {
            return new ResponseEntity<>(core.create(transaction), HttpStatus.CREATED);
        } catch (Throwable t) {
            log.error("Error on create");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> find(@PathVariable String id) {
        try {
            Optional<TransactionResponse> optional = core.findById(id);

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
    public ResponseEntity<List<TransactionResponse>> list() {
        try {
            List<TransactionResponse> transactions = core.findAll();
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        } catch (Throwable t) {
            log.error("Error on list");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@PathVariable String id, @RequestBody @Valid TransactionRequest transaction) {
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
