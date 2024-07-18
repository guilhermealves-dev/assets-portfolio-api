package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.UserMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.UserRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.UserResponse;
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
        try {
            UserDocument docRequest = userMapper.map(user);
            UserDocument doc = userFireBaseAdapter.create(docRequest);

            return userMapper.map(doc);

        } catch (Throwable t){
            log.error("Error on create - {}", t.getMessage());
            throw t;
        }
    }

    public UserResponse update(UserRequest user) throws Exception {
        try {
            UserDocument docRequest = userMapper.map(user);
            UserDocument doc = userFireBaseAdapter.update(docRequest);

            return userMapper.map(doc);

        } catch (Throwable t){
            log.error("Error on update - {}", t.getMessage());
            throw t;
        }
    }

    public List<UserResponse> findAll() throws Exception {
        try {
            List<UserDocument> userDocs = userFireBaseAdapter.findAll();

            return userMapper.mapList(userDocs);

        } catch (Throwable t){
            log.error("Error on find All - {}", t.getMessage());
            throw t;
        }
    }

    public Optional<UserResponse> findById(String id) throws Exception {
        try {
            Optional<UserDocument> optionalDoc = userFireBaseAdapter.findById(id);

            if(optionalDoc.isPresent()){
                return Optional.of(userMapper.map(optionalDoc.get()));
            }

            return Optional.empty();

        } catch (Throwable t){
            log.error("Error on find By Id - {}", t.getMessage());
            throw t;
        }
    }

    public void deleteById(String id) throws Exception {
        try {
            userFireBaseAdapter.deleteById(id);
        } catch (Throwable t){
            log.error("Error on delete By Id - {}", t.getMessage());
            throw t;
        }
    }
}
