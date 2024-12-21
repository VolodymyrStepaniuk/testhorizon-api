package com.stepaniuk.testhorizon.testcase;

import com.stepaniuk.testhorizon.payload.testcase.TestCaseResponse;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TestCaseMapper {

    @Mapping(target = "priority", source = "testCase.priority.name")
    @BeanMapping(qualifiedByName = "addLinks")
    TestCaseResponse toResponse(TestCase testCase);

    @AfterMapping
    @Named("addLinks")
    default TestCaseResponse addLinks(@MappingTarget TestCaseResponse response) {

        response.add(Link.of("/test-cases/{id}").withSelfRel());
        response.add(Link.of("/test-cases/{id}").withRel("update"));
        response.add(Link.of("/test-cases/{id}").withRel("delete"));

        return response;
    }
}
