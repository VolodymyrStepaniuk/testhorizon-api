package com.stepaniuk.testhorizon.payload.bugreport;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityName;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatusName;
import com.stepaniuk.testhorizon.validation.bugreport.Environment;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.ImageUrl;
import com.stepaniuk.testhorizon.validation.shared.Title;
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

    @Id
    @NotNull
    private Long id;

    @Id
    @NotNull
    private Long projectId;

    @Id
    @NotNull
    private Long reporterId;

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
    private List<@ImageUrl String> imageUrls;

    @NotNull
    private BugReportSeverityName severity;

    @NotNull
    private BugReportStatusName status;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
