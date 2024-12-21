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
    default BugReportResponse addLinks(@MappingTarget BugReportResponse response) {

        response.add(Link.of("/bug-reports/{id}").withSelfRel());
        response.add(Link.of("/bug-reports/{id}").withRel("update"));
        response.add(Link.of("/bug-reports/{id}").withRel("delete"));

        return response;
    }
}
