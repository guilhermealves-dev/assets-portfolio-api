package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.adapters.out.UserFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.UserMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.User;
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

    public User create(UserDocument userDoc) throws Exception {
        try {
            UserDocument doc = userFireBaseAdapter.create(userDoc);

            return userMapper.mapper(doc);

        } catch (Throwable t){
            log.error("Error on create - {}", t.getMessage());
            throw t;
        }
    }

    public User update(UserDocument userDoc) throws Exception {
        try {
            UserDocument doc = userFireBaseAdapter.update(userDoc);

            return userMapper.mapper(doc);

        } catch (Throwable t){
            log.error("Error on update - {}", t.getMessage());
            throw t;
        }
    }

    public List<User> findAll() throws Exception {
        try {
            List<UserDocument> userDocs = userFireBaseAdapter.findAll();

            return userMapper.mapperUserList(userDocs);

        } catch (Throwable t){
            log.error("Error on find All - {}", t.getMessage());
            throw t;
        }
    }

    public Optional<User> findById(String id) throws Exception {
        try {
            Optional<UserDocument> optionalDoc = userFireBaseAdapter.findById(id);

            if(optionalDoc.isPresent()){
                return Optional.of(userMapper.mapper(optionalDoc.get()));
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
