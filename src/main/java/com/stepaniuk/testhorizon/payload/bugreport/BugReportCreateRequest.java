package com.stepaniuk.testhorizon.payload.bugreport;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityName;
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

    @NotNull
    private Long projectId;

    @NotNull
    private String title;

    @NotNull
    private String description;

    @NotNull
    private String environment;

    @NotNull
    private List<String> imageUrls;

    @NotNull
    private BugReportSeverityName severity;
}
