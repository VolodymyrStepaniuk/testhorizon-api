package com.stepaniuk.testhorizon.rating;

import com.stepaniuk.testhorizon.payload.rating.RatingResponse;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface RatingMapper {

    @BeanMapping(qualifiedByName = "addLinks")
    RatingResponse toResponse(Rating rating);

    @AfterMapping
    @Named("addLinks")
    default RatingResponse addLinks(Rating rating, @MappingTarget RatingResponse response) {

        response.add(Link.of("/ratings/" + rating.getId()).withSelfRel());

        return response;
    }
}
