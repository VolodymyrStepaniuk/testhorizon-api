package com.stepaniuk.testhorizon.export.handler;

import com.stepaniuk.testhorizon.payload.test.TestResponse;
import com.stepaniuk.testhorizon.test.TestService;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.stepaniuk.testhorizon.export.ExportUtils.safeForCsv;
import static com.stepaniuk.testhorizon.export.ExportUtils.safeForXml;
import static com.stepaniuk.testhorizon.types.entity.EntityType.TEST;

@Component
public class TestExportHandler implements EntityExportHandler<TestResponse> {

    private final TestService testService;

    @Autowired
    public TestExportHandler(TestService testService) {
        this.testService = testService;
    }

    @Override
    public EntityType getEntityType() {
        return TEST;
    }

    @Override
    public TestResponse findById(Long id) {
        return testService.getTestById(id);
    }

    @Override
    public String toCsv(TestResponse test) {
        if (test == null) return "";

        String header = "ProjectId,TestCaseId,Title,Description,Instructions,GithubUrl,Type";

        String dataRow = String.join(",",
                safeForCsv(test.getProject().getId() != null ? test.getProject().getId().toString() : ""),
                safeForCsv(test.getTestCase().getId() != null ? test.getTestCase().getId().toString() : ""),
                safeForCsv(test.getTitle()),
                safeForCsv(test.getDescription()),
                safeForCsv(test.getInstructions()),
                safeForCsv(test.getGithubUrl()),
                (test.getType() != null) ? test.getType().name() : ""
        );

        return header + "\n" + dataRow;
    }

    @Override
    public String toXml(TestResponse test) {
        if (test == null) {
            return "<test></test>";
        }

        return """
                <test>
                    <projectId>%s</projectId>
                    <testCaseId>%s</testCaseId>
                    <title>%s</title>
                    <description>%s</description>
                    <instructions>%s</instructions>
                    <githubUrl>%s</githubUrl>
                    <type>%s</type>
                </test>
                """.formatted(
                (test.getProject().getId() != null) ? test.getProject().getId().toString() : "",
                (test.getTestCase().getId() != null) ? test.getTestCase().getId() : "",
                safeForXml(test.getTitle()),
                safeForXml(test.getDescription()),
                safeForXml(test.getInstructions()),
                safeForXml(test.getGithubUrl()),
                (test.getType() != null) ? test.getType().name() : ""
        );
    }
}
