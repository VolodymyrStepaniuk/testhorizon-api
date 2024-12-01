package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.payload.user.UserResponse;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {

    @BeanMapping(qualifiedByName = "addLinks")
    UserResponse toResponse(User user);

    @AfterMapping
    @Named("addLinks")
    default UserResponse addLinks(@MappingTarget UserResponse response) {

        response.add(Link.of("/users/{id}").withSelfRel());
        response.add(Link.of("/users/{id}").withRel("update"));
        response.add(Link.of("/users/{id}").withRel("delete"));

        return response;
    }
}
