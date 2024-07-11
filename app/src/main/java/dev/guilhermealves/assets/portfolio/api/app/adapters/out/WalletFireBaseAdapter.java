package dev.guilhermealves.assets.portfolio.api.app.adapters.out;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.guilhermealves.assets.portfolio.api.app.domain.entity.Wallet;
import dev.guilhermealves.assets.portfolio.api.app.ports.out.DataBaseIntegration;

@Service
public class WalletFireBaseAdapter implements DataBaseIntegration<Wallet, String> {

    @Override
    public Wallet save(Wallet t) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public Optional<Wallet> findById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public List<Wallet> findAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAll'");
    }

    @Override
    public void deleteById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }

}
