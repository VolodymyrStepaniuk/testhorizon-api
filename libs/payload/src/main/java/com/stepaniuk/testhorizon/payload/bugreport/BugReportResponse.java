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
    private Long id;

    @NotNull
    private ProjectInfo project;

    @NotNull
    private UserInfo reporter;

    @Title
    @NotNull
    private String title;

    @Description
    @NotNull
    private String description;

    @Environment
    @NotNull
    private String environment;

    @NotNull
    private BugReportSeverityName severity;

    @NotNull
    private BugReportStatusName status;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
