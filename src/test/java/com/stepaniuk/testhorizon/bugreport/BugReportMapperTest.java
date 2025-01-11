package com.stepaniuk.testhorizon.bugreport;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverity;
import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityName;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatus;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatusName;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportResponse;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MapperLevelUnitTest
@ContextConfiguration(classes = {BugReportMapperImpl.class})
class BugReportMapperTest {

    @Autowired
    private BugReportMapper bugReportMapper;

    @Test
    void shouldMapBugReportToBugReportResponse(){
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        BugReportSeverity bugReportSeverity = new BugReportSeverity(1L, BugReportSeverityName.CRITICAL);
        BugReportStatus bugReportStatus = new BugReportStatus(1L, BugReportStatusName.OPENED);
        BugReport bugReport = new BugReport(null, 1L,  "Bug report title", "Bug report description",
                "Bug report environment", 1L, List.of(), bugReportSeverity, bugReportStatus, timeOfCreation, timeOfModification);

        // when
        BugReportResponse bugReportResponse = bugReportMapper.toResponse(bugReport);

        // then
        assertNotNull(bugReportResponse);
        assertNull(bugReportResponse.getId());
        assertEquals(bugReport.getProjectId(), bugReportResponse.getProjectId());
        assertEquals(bugReport.getTitle(), bugReportResponse.getTitle());
        assertEquals(bugReport.getDescription(), bugReportResponse.getDescription());
        assertEquals(bugReport.getEnvironment(), bugReportResponse.getEnvironment());
        assertEquals(bugReport.getReporterId(), bugReportResponse.getReporterId());
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
