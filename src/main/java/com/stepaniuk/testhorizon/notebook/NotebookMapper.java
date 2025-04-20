package com.stepaniuk.testhorizon.notebook;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.notebook.NotebookResponse;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface NotebookMapper {

    @Mapping(target = "owner.id", source = "userInfo.id")
    @Mapping(target = "owner.firstName", source = "userInfo.firstName")
    @Mapping(target = "owner.lastName", source = "userInfo.lastName")
    @Mapping(target = "id", source = "notebook.id")
    @BeanMapping(qualifiedByName = "addLinks")
    NotebookResponse toResponse(Notebook notebook, UserInfo userInfo);

    @AfterMapping
    @Named("addLinks")
    default NotebookResponse addLinks(Notebook notebook, @MappingTarget NotebookResponse response) {

        response.add(Link.of("/notebooks/" + notebook.getId()).withSelfRel());
        response.add(Link.of("/notebooks/" + notebook.getId()).withRel("update"));
        response.add(Link.of("/notebooks/" + notebook.getId()).withRel("delete"));

        return response;
    }
}