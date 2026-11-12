package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.UserMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.UserRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.UserResponse;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCore {

    private final UserFireBaseAdapter userFireBaseAdapter;
    private final UserMapper userMapper;

    public UserResponse create(UserRequest user) throws Exception {
        UserDocument docRequest = userMapper.map(user);
        UserDocument doc = userFireBaseAdapter.create(docRequest);
        return userMapper.map(doc);
    }

    public UserResponse update(UserRequest user) throws Exception {
        userFireBaseAdapter.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", user.getId()));

        UserDocument docRequest = userMapper.map(user);
        UserDocument doc = userFireBaseAdapter.update(docRequest);
        return userMapper.map(doc);
    }

    public List<UserResponse> findAll() throws Exception {
        List<UserDocument> userDocs = userFireBaseAdapter.findAll();
        return userMapper.mapList(userDocs);
    }

    public Optional<UserResponse> findById(String id) throws Exception {
        Optional<UserDocument> optionalDoc = userFireBaseAdapter.findById(id);
        if (optionalDoc.isPresent()) {
            return Optional.of(userMapper.map(optionalDoc.get()));
        }
        return Optional.empty();
    }

    public void deleteById(String id) throws Exception {
        userFireBaseAdapter.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));

        userFireBaseAdapter.deleteById(id);
    }
}
