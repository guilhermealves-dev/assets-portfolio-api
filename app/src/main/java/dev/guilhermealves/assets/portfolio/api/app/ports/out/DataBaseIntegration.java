package dev.guilhermealves.assets.portfolio.api.app.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface DataBaseIntegration<T, ID> {
    public T save(T t);

    public Optional<T> findById(ID id) throws ExecutionException, InterruptedException;

    public List<T> findAll() throws ExecutionException, InterruptedException;

    public void deleteById(ID id);
}
