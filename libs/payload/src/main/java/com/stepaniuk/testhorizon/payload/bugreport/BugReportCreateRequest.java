package com.stepaniuk.testhorizon.payload.bugreport;


import com.stepaniuk.testhorizon.types.bugreport.BugReportSeverityName;
import com.stepaniuk.testhorizon.validation.bugreport.Environment;

import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Title;
import com.stepaniuk.testhorizon.validation.shared.Description;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
    private BugReportSeverityName severity;
}
