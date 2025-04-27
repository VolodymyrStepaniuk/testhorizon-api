package com.stepaniuk.testhorizon.payload.bugreport;


import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.validation.bugreport.Environment;
import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Title;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.types.bugreport.BugReportSeverityName;
import com.stepaniuk.testhorizon.types.bugreport.BugReportStatusName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;


@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "bugReports", itemRelation = "bugReports")
public class BugReportResponse extends RepresentationModel<BugReportResponse> {

    @Id
    @NotNull
    private final Long id;

    @NotNull
    private final ProjectInfo project;

    @NotNull
    private final UserInfo reporter;

    @Title
    @NotNull
    private final String title;

    @Description
    @NotNull
    private final String description;

    @Environment
    @NotNull
    private final String environment;

    @NotNull
    private final BugReportSeverityName severity;

    @NotNull
    private final BugReportStatusName status;

    @NotNull
    private final Instant createdAt;

    @NotNull
    private final Instant updatedAt;
}
