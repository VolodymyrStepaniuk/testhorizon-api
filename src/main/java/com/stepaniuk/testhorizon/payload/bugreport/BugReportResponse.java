package com.stepaniuk.testhorizon.payload.bugreport;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityName;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatusName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;
import java.util.List;


@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "bugReports", itemRelation = "bugReports")
public class BugReportResponse extends RepresentationModel<BugReportResponse> {

    @NotNull
    private Long id;

    @NotNull
    private Long projectId;

    @NotNull
    private String title;

    @NotNull
    private String description;

    @NotNull
    private String environment;

    @NotNull
    private Long reporterId;

    @NotNull
    private List<String> imageUrls;

    @NotNull
    private BugReportSeverityName severity;

    @NotNull
    private BugReportStatusName status;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
