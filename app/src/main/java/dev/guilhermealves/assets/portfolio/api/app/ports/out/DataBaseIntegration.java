package dev.guilhermealves.assets.portfolio.api.app.ports.out;

import java.util.List;
import java.util.Optional;

public interface DataBaseIntegration<T, ID> {
    public T create(T t) throws Exception;

    public T update(T t) throws Exception;

    public Optional<T> findById(ID id) throws Exception;

    public List<T> findAll() throws Exception;

    public void deleteById(ID id) throws Exception;
}
