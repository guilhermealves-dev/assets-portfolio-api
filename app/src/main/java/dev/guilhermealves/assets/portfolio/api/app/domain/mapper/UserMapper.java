package dev.guilhermealves.assets.portfolio.api.app.domain.mapper;

import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapper(UserDocument document);

    UserDocument mapper(User user);

    List<User> mapperUserList(List<UserDocument> documents);

    List<UserDocument> mapperDocList(List<User> users);
}
