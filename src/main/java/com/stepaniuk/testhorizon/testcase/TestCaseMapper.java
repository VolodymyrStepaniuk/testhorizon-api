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
    default TestCaseResponse addLinks(TestCase testCase, @MappingTarget TestCaseResponse response) {

        response.add(Link.of("/test-cases/" + testCase.getId()).withSelfRel());
        response.add(Link.of("/test-cases/" + testCase.getId()).withRel("update"));
        response.add(Link.of("/test-cases/" + testCase.getId()).withRel("delete"));

        return response;
    }
}
