package com.stepaniuk.testhorizon.export.handler;

import com.stepaniuk.testhorizon.bugreport.BugReportService;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportResponse;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.stepaniuk.testhorizon.export.ExportUtils.safeForCsv;
import static com.stepaniuk.testhorizon.export.ExportUtils.safeForXml;
import static com.stepaniuk.testhorizon.types.entity.EntityType.BUG_REPORT;

@Component
public class BugReportExportHandler implements EntityExportHandler<BugReportResponse> {

    private final BugReportService bugReportService;

    @Autowired
    public BugReportExportHandler(BugReportService bugReportService) {
        this.bugReportService = bugReportService;
    }

    @Override
    public EntityType getEntityType() {
        return BUG_REPORT;
    }

    @Override
    public BugReportResponse findById(Long id) {
        return bugReportService.getBugReportById(id);
    }

    @Override
    public String toCsv(BugReportResponse bugReportResponse) {
        if (bugReportResponse == null) return "";

        String header = "ProjectId,Title,Description,Environment,Severity";

        String dataRow = String.join(",",
                safeForCsv(bugReportResponse.getProject().getId() != null ? bugReportResponse.getProject().getId().toString() : ""),
                safeForCsv(bugReportResponse.getTitle()),
                safeForCsv(bugReportResponse.getDescription()),
                safeForCsv(bugReportResponse.getEnvironment()),
                (bugReportResponse.getSeverity() != null) ? bugReportResponse.getSeverity().name() : ""
        );

        return header + "\n" + dataRow;
    }

    @Override
    public String toXml(BugReportResponse bugReportResponse) {
        if (bugReportResponse == null) {
            return "<bugreport></bugreport>";
        }

        return """
                <bugreport>
                    <projectId>%s</projectId>
                    <title>%s</title>
                    <description>%s</description>
                    <environment>%s</environment>
                    <severity>%s</severity>
                </bugreport>
                """.formatted(
                (bugReportResponse.getProject().getId() != null ? bugReportResponse.getProject().getId().toString() : ""),
                safeForXml(bugReportResponse.getTitle()),
                safeForXml(bugReportResponse.getDescription()),
                safeForXml(bugReportResponse.getEnvironment()),
                (bugReportResponse.getSeverity() != null) ? bugReportResponse.getSeverity().name() : ""
        );
    }
}
