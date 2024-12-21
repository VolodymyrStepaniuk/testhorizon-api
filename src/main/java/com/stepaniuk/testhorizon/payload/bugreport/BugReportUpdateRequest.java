package com.stepaniuk.testhorizon.payload.bugreport;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityName;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatusName;
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

    @Nullable
    private String title;

    @Nullable
    private String description;

    @Nullable
    private String environment;

    @Nullable
    private List<String> imageUrls;

    @Nullable
    private BugReportSeverityName severity;

    @Nullable
    private BugReportStatusName status;
}
