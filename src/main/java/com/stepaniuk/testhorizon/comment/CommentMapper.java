package com.stepaniuk.testhorizon.comment;

import com.stepaniuk.testhorizon.payload.comment.CommentResponse;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CommentMapper {

    @Mapping(target = "author.id", source = "userInfo.id")
    @Mapping(target = "author.firstName", source = "userInfo.firstName")
    @Mapping(target = "author.lastName", source = "userInfo.lastName")
    @Mapping(target = "id", source = "comment.id")
    @BeanMapping(qualifiedByName = "addLinks")
    CommentResponse toResponse(Comment comment, UserInfo userInfo);

    @AfterMapping
    @Named("addLinks")
    default CommentResponse addLinks(Comment comment, @MappingTarget CommentResponse response) {

        response.add(Link.of("/comments/" + comment.getId()).withSelfRel());
        response.add(Link.of("/comments/" + comment.getId()).withRel("update"));
        response.add(Link.of("/comments/" + comment.getId()).withRel("delete"));

        return response;
    }
}
