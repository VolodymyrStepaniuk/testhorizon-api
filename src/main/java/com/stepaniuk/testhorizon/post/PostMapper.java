package com.stepaniuk.testhorizon.post;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.post.PostResponse;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PostMapper {

    @Mapping(target = "category", source = "post.category.name")
    @Mapping(target = "owner.id", source = "userInfo.id")
    @Mapping(target = "owner.firstName", source = "userInfo.firstName")
    @Mapping(target = "owner.lastName", source = "userInfo.lastName")
    @Mapping(target = "id", source = "post.id")
    @BeanMapping(qualifiedByName = "addLinks")
    PostResponse toResponse(Post post, UserInfo userInfo);

    @AfterMapping
    @Named("addLinks")
    default PostResponse addLinks(Post post, @MappingTarget PostResponse response) {
        response.add(Link.of("/posts/" + post.getId()).withSelfRel());
        response.add(Link.of("/posts/" + post.getId()).withRel("update"));
        response.add(Link.of("/posts/" + post.getId()).withRel("delete"));

        return response;
    }
}
