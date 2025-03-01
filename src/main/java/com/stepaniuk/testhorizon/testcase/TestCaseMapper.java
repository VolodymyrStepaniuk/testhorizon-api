package com.stepaniuk.testhorizon.testcase;

import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseResponse;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TestCaseMapper {

    @Mapping(target = "priority", source = "testCase.priority.name")
    @Mapping(target = "project.id", source = "projectInfo.id")
    @Mapping(target = "project.title", source = "projectInfo.title")
    @Mapping(target = "author.id", source = "userInfo.id")
    @Mapping(target = "author.firstName", source = "userInfo.firstName")
    @Mapping(target = "author.lastName", source = "userInfo.lastName")
    @Mapping(target = "id", source = "testCase.id")
    @Mapping(target = "title", source = "testCase.title")
    @BeanMapping(qualifiedByName = "addLinks")
    TestCaseResponse toResponse(TestCase testCase, ProjectInfo projectInfo, UserInfo userInfo);

    @AfterMapping
    @Named("addLinks")
    default TestCaseResponse addLinks(TestCase testCase, @MappingTarget TestCaseResponse response) {

        response.add(Link.of("/test-cases/" + testCase.getId()).withSelfRel());
        response.add(Link.of("/test-cases/" + testCase.getId()).withRel("update"));
        response.add(Link.of("/test-cases/" + testCase.getId()).withRel("delete"));

        return response;
    }
}
