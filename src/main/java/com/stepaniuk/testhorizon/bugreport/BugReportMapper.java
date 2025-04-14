package com.stepaniuk.testhorizon.bugreport;

import com.stepaniuk.testhorizon.payload.bugreport.BugReportResponse;
import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BugReportMapper {

    @Mapping(target = "status", source = "bugReport.status.name")
    @Mapping(target = "severity", source = "bugReport.severity.name")
    @Mapping(target = "project.id", source = "projectInfo.id")
    @Mapping(target = "project.title", source = "projectInfo.title")
    @Mapping(target = "project.ownerId", source = "projectInfo.ownerId")
    @Mapping(target = "reporter.id", source = "userInfo.id")
    @Mapping(target = "reporter.firstName", source = "userInfo.firstName")
    @Mapping(target = "reporter.lastName", source = "userInfo.lastName")
    @Mapping(target = "id", source = "bugReport.id")
    @Mapping(target = "title", source = "bugReport.title")
    @BeanMapping(qualifiedByName = "addLinks")
    BugReportResponse toResponse(BugReport bugReport, ProjectInfo projectInfo, UserInfo userInfo);

    @AfterMapping
    @Named("addLinks")
    default BugReportResponse addLinks(BugReport bugReport, @MappingTarget BugReportResponse response) {

        response.add(Link.of("/bug-reports/" + bugReport.getId()).withSelfRel());
        response.add(Link.of("/bug-reports/"  + bugReport.getId()).withRel("update"));
        response.add(Link.of("/bug-reports/"  + bugReport.getId()).withRel("delete"));

        return response;
    }
}
