package dev.guilhermealves.assets.portfolio.api.app.adapters.in;

import dev.guilhermealves.assets.portfolio.api.app.domain.core.UserCore;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.User;
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
@RequestMapping("users")
public class UserControllerAdapter implements ControllerIntegration<User, UserDocument, String> {

    private final UserCore core;

    @Override
    @PostMapping
    public ResponseEntity<User> create(@RequestBody @Valid UserDocument user) {
        try {
            return new ResponseEntity<>(core.create(user), HttpStatus.CREATED);
        } catch (Throwable t) {
            log.error("Error on create");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<User> find(@PathVariable String id) {
        try {
            Optional<User> optional = core.findById(id);

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
    public ResponseEntity<List<User>> list() {
        try {
            List<User> users = core.findAll();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Throwable t) {
            log.error("Error on list");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable String id, @RequestBody @Valid UserDocument user) {
        try {
            user.setId(id);
            return new ResponseEntity<>(core.update(user), HttpStatus.ACCEPTED);

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
