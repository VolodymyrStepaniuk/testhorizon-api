package com.stepaniuk.testhorizon.payload.bugreport;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityName;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatusName;
import com.stepaniuk.testhorizon.validation.bugreport.Environment;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.ImageUrl;
import com.stepaniuk.testhorizon.validation.shared.Title;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class BugReportUpdateRequest {

    @Title
    @Nullable
    private String title;

    @Description
    @Nullable
    private String description;

    @Environment
    @Nullable
    private String environment;

    @Nullable
    private List<@ImageUrl String> imageUrls;

    @Nullable
    private BugReportSeverityName severity;

    @Nullable
    private BugReportStatusName status;
}
