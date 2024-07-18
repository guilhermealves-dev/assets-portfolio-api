package dev.guilhermealves.assets.portfolio.api.app.domain.mapper;

import dev.guilhermealves.assets.portfolio.api.app.domain.entity.UserDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.UserRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDocument map(UserRequest request);

    UserResponse map(UserDocument document);

    List<UserResponse> mapList(List<UserDocument> documents);
}
