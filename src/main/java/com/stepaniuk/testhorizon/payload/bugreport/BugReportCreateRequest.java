package com.stepaniuk.testhorizon.payload.bugreport;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityName;
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

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BugReportCreateRequest {

    @Id
    @NotNull
    private Long projectId;

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
}
