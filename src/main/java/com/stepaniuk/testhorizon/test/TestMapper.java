package com.stepaniuk.testhorizon.test;

import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.TestCaseInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.test.TestResponse;
import jakarta.annotation.Nullable;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TestMapper {

    @Mapping(target = "type", source = "test.type.name")
    @Mapping(target = "project.id", source = "projectInfo.id")
    @Mapping(target = "project.title", source = "projectInfo.title")
    @Mapping(target = "project.ownerId", source = "projectInfo.ownerId")
    @Mapping(target = "author.id", source = "userInfo.id")
    @Mapping(target = "author.firstName", source = "userInfo.firstName")
    @Mapping(target = "author.lastName", source = "userInfo.lastName")
    @Mapping(target = "testCase.id", source = "testCaseInfo.id")
    @Mapping(target = "testCase.title", source = "testCaseInfo.title")
    @Mapping(target = "id", source = "test.id")
    @Mapping(target = "title", source = "test.title")
    @BeanMapping(qualifiedByName = "addLinks")
    TestResponse toResponse(Test test, ProjectInfo projectInfo, UserInfo userInfo, @Nullable TestCaseInfo testCaseInfo);

    @AfterMapping
    @Named("addLinks")
    default TestResponse addLinks(Test test, @MappingTarget TestResponse response) {

        response.add(Link.of("/tests/" + test.getId()).withSelfRel());
        response.add(Link.of("/tests/" + test.getId()).withRel("update"));
        response.add(Link.of("/tests/" + test.getId()).withRel("delete"));

        return response;
    }
}
