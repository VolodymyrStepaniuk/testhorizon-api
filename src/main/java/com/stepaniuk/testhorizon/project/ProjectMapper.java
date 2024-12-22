package com.stepaniuk.testhorizon.project;

import com.stepaniuk.testhorizon.payload.project.ProjectResponse;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ProjectMapper {

    @Mapping(target = "status", source = "project.status.name")
    @BeanMapping(qualifiedByName = "addLinks")
    ProjectResponse toResponse(Project project);

    @AfterMapping
    @Named("addLinks")
    default ProjectResponse addLinks(Project project, @MappingTarget ProjectResponse response) {

        response.add(Link.of("/projects/" + project.getId()).withSelfRel());
        response.add(Link.of("/projects/" + project.getId()).withRel("update"));
        response.add(Link.of("/projects/" + project.getId()).withRel("delete"));

        return response;
    }
}
