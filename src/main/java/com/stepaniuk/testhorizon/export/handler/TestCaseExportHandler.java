package com.stepaniuk.testhorizon.export.handler;

import com.stepaniuk.testhorizon.export.ExportUtils;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseResponse;
import com.stepaniuk.testhorizon.testcase.TestCaseService;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static com.stepaniuk.testhorizon.export.ExportUtils.safeForCsv;
import static com.stepaniuk.testhorizon.export.ExportUtils.safeForXml;
import static com.stepaniuk.testhorizon.types.entity.EntityType.TEST_CASE;

@Component
public class TestCaseExportHandler implements EntityExportHandler<TestCaseResponse> {

    private final TestCaseService testCaseService;

    @Autowired
    public TestCaseExportHandler(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }

    @Override
    public EntityType getEntityType() {
        return TEST_CASE;
    }

    @Override
    public TestCaseResponse findById(Long id) {
        return testCaseService.getTestCaseById(id);
    }

    @Override
    public String toCsv(TestCaseResponse testCase) {
        if (testCase == null) return "";

        String header = "ProjectId,Title,Description,Preconditions,InputData,Steps,Priority";

        String joinedSteps = "";
        if (testCase.getSteps() != null && !testCase.getSteps().isEmpty()) {
            joinedSteps = testCase.getSteps().stream()
                    .map(ExportUtils::safeForCsv)
                    .collect(Collectors.joining(";"));
        }

        String dataRow = String.join(",",
                safeForCsv(testCase.getProject().getId() != null ? testCase.getProject().getId().toString() : ""),
                safeForCsv(testCase.getTitle()),
                safeForCsv(testCase.getDescription()),
                safeForCsv(testCase.getDescription()),
                safeForCsv(testCase.getPreconditions()),
                safeForCsv(testCase.getInputData()),
                safeForCsv(joinedSteps),
                (testCase.getPriority() != null) ? testCase.getPriority().name() : ""
        );

        return header + "\n" + dataRow;
    }

    @Override
    public String toXml(TestCaseResponse testCase) {
        if (testCase == null) {
            return "<testcase></testcase>";
        }

        String stepsXml = "";

        if (testCase.getSteps() != null && !testCase.getSteps().isEmpty()) {
            stepsXml = testCase.getSteps().stream()
                    .map(step -> "<step>" + safeForXml(step) + "</step>")
                    .collect(Collectors.joining(""));
        }

        return """
                <testcase>
                    <projectId>%s</projectId>
                    <title>%s</title>
                    <description>%s</description>
                    <preconditions>%s</preconditions>
                    <inputData>%s</inputData>
                    <steps>%s</steps>
                    <priority>%s</priority>
                </testcase>
                """.formatted(
                (testCase.getProject().getId() != null) ? testCase.getProject().getId() : "",
                safeForXml(testCase.getTitle()),
                safeForXml(testCase.getDescription()),
                safeForXml(testCase.getPreconditions()),
                safeForXml(testCase.getInputData()),
                stepsXml,
                (testCase.getPriority() != null) ? testCase.getPriority().name() : ""
        );
    }
}
