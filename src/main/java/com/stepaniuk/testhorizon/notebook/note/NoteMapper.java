package com.stepaniuk.testhorizon.notebook.note;

import com.stepaniuk.testhorizon.payload.notebook.note.NoteResponse;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface NoteMapper {

    @BeanMapping(qualifiedByName = "addLinks")
    NoteResponse toResponse(Note note);

    @AfterMapping
    @Named("addLinks")
    default NoteResponse addLinks(Note note, @MappingTarget NoteResponse response) {
        response.add(Link.of("/notes/" + note.getId()).withSelfRel());
        response.add(Link.of("/notes/" + note.getId()).withRel("update"));
        response.add(Link.of("/notes/" + note.getId()).withRel("delete"));
        return response;
    }
}
