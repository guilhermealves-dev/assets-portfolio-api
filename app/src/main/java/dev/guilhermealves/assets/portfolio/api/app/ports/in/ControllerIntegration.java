package dev.guilhermealves.assets.portfolio.api.app.ports.in;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ControllerIntegration<T, R, ID> {

    public ResponseEntity<R> create(T t);

    public ResponseEntity<R> find(ID id);

    public ResponseEntity<List<R>> list();

    public ResponseEntity<R> update(ID id, T t);

    public ResponseEntity<?> delete(ID id);
}
