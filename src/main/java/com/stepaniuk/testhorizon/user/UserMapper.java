package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import jakarta.annotation.Nullable;
import org.mapstruct.*;
import org.springframework.hateoas.Link;
import org.springframework.security.core.GrantedAuthority;

import java.util.stream.Collectors;

import static com.stepaniuk.testhorizon.security.SecurityUtils.hasAuthority;
import static com.stepaniuk.testhorizon.security.SecurityUtils.isOwner;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {Collectors.class, AuthorityName.class, GrantedAuthority.class}
)

public interface UserMapper {

    @Mapping(target = "authorities", expression = "java(user.getAuthorities().stream()" +
            ".map(GrantedAuthority::getAuthority)" +
            ".map(AuthorityName::valueOf)" +
            ".collect(Collectors.toSet()))")
    @BeanMapping(qualifiedByName = "addLinks")
    UserResponse toResponse(User user, @Nullable AuthInfo authInfo);

    @AfterMapping
    @Named("addLinks")
    default UserResponse addLinks(User user, @MappingTarget UserResponse response, @Nullable AuthInfo authInfo) {

        response.add(Link.of("/users/" + user.getId()).withSelfRel());
        response.add(Link.of("/users/" + user.getEmail()).withSelfRel());

        if(authInfo!= null && hasAuthority(authInfo, AuthorityName.ADMIN.name())) {
            response.add(Link.of("/users/" + user.getId()).withRel("update"));
            response.add(Link.of("/users/" + user.getId()).withRel("delete"));
        }

        if(authInfo!= null && isOwner(authInfo, user.getId())) {
            response.add(Link.of("/users/me").withSelfRel());
            response.add(Link.of("/users/me").withRel("update"));
            response.add(Link.of("/users/me").withRel("delete"));
        }

        return response;
    }
}
