package dev.guilhermealves.assets.portfolio.api.app.ports.in;

import java.util.List;

import org.springframework.http.ResponseEntity;

public interface ControllerIntegration<T, ID> {

    public ResponseEntity<T> create(T t);

    public ResponseEntity<T> find(ID id);

    public ResponseEntity<List<T>> list();

    public ResponseEntity<T> update(ID id, T t);

    public ResponseEntity<?> delete(ID id);
}
