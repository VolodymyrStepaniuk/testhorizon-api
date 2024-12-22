package com.stepaniuk.testhorizon.test;

import com.stepaniuk.testhorizon.payload.test.TestResponse;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TestMapper {

    @Mapping(target = "type", source = "test.type.name")
    @BeanMapping(qualifiedByName = "addLinks")
    TestResponse toResponse(Test test);

    @AfterMapping
    @Named("addLinks")
    default TestResponse addLinks(Test test, @MappingTarget TestResponse response) {

        response.add(Link.of("/tests/" + test.getId()).withSelfRel());
        response.add(Link.of("/tests/" + test.getId()).withRel("update"));
        response.add(Link.of("/tests/" + test.getId()).withRel("delete"));

        return response;
    }
}
