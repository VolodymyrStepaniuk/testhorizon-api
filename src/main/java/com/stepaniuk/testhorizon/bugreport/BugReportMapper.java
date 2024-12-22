package com.stepaniuk.testhorizon.bugreport;

import com.stepaniuk.testhorizon.payload.bugreport.BugReportResponse;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BugReportMapper {

    @Mapping(target = "status", source = "bugReport.status.name")
    @Mapping(target = "severity", source = "bugReport.severity.name")
    @BeanMapping(qualifiedByName = "addLinks")
    BugReportResponse toResponse(BugReport bugReport);

    @AfterMapping
    @Named("addLinks")
    default BugReportResponse addLinks(BugReport bugReport, @MappingTarget BugReportResponse response) {

        response.add(Link.of("/bug-reports/" + bugReport.getId()).withSelfRel());
        response.add(Link.of("/bug-reports/"  + bugReport.getId()).withRel("update"));
        response.add(Link.of("/bug-reports/"  + bugReport.getId()).withRel("delete"));

        return response;
    }
}
