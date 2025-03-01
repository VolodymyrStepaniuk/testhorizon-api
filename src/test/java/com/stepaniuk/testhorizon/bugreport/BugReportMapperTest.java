package com.stepaniuk.testhorizon.bugreport;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverity;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatus;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportResponse;
import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import com.stepaniuk.testhorizon.types.bugreport.BugReportSeverityName;
import com.stepaniuk.testhorizon.types.bugreport.BugReportStatusName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@MapperLevelUnitTest
@ContextConfiguration(classes = {BugReportMapperImpl.class})
class BugReportMapperTest {

    @Autowired
    private BugReportMapper bugReportMapper;

    @Test
    void shouldMapBugReportToBugReportResponse() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        BugReportSeverity bugReportSeverity = new BugReportSeverity(1L, BugReportSeverityName.CRITICAL);
        BugReportStatus bugReportStatus = new BugReportStatus(1L, BugReportStatusName.OPENED);
        BugReport bugReport = new BugReport(null, 1L, "Bug report title", "Bug report description",
                "Bug report environment", 1L,
                bugReportSeverity, bugReportStatus, timeOfCreation, timeOfModification);
        var userInfo = new UserInfo(1L, "John", "Doe");
        var projectInfo = new ProjectInfo(1L, "Project title");

        // when
        BugReportResponse bugReportResponse = bugReportMapper.toResponse(bugReport, projectInfo, userInfo);

        // then
        assertNotNull(bugReportResponse);
        assertNull(bugReportResponse.getId());
        assertNotNull(bugReportResponse.getProject());
        assertEquals(bugReport.getProjectId(), bugReportResponse.getProject().getId());
        assertEquals(projectInfo.getTitle(), bugReportResponse.getProject().getTitle());
        assertEquals(bugReport.getTitle(), bugReportResponse.getTitle());
        assertEquals(bugReport.getDescription(), bugReportResponse.getDescription());
        assertEquals(bugReport.getEnvironment(), bugReportResponse.getEnvironment());
        assertNotNull(bugReportResponse.getReporter());
        assertEquals(bugReport.getReporterId(), bugReportResponse.getReporter().getId());
        assertEquals(userInfo.getFirstName(), bugReportResponse.getReporter().getFirstName());
        assertEquals(userInfo.getLastName(), bugReportResponse.getReporter().getLastName());
        assertEquals(bugReport.getSeverity().getName(), bugReportResponse.getSeverity());
        assertEquals(bugReport.getStatus().getName(), bugReportResponse.getStatus());
        assertEquals(bugReport.getCreatedAt(), bugReportResponse.getCreatedAt());
        assertEquals(bugReport.getUpdatedAt(), bugReportResponse.getUpdatedAt());
        assertTrue(bugReportResponse.hasLinks());
        assertTrue(bugReportResponse.getLinks().hasLink("self"));
        assertTrue(bugReportResponse.getLinks().hasLink("update"));
        assertTrue(bugReportResponse.getLinks().hasLink("delete"));
    }
}
