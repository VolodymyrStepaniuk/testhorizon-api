package com.stepaniuk.testhorizon.export.handler;

import com.stepaniuk.testhorizon.bugreport.BugReportService;
import com.stepaniuk.testhorizon.payload.bugreport.BugReportResponse;
import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.types.bugreport.BugReportSeverityName;
import com.stepaniuk.testhorizon.types.bugreport.BugReportStatusName;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BugReportExportHandlerTest {

    @Mock
    private BugReportService bugReportService;

    private BugReportExportHandler bugReportExportHandler;

    @BeforeEach
    void setUp() {
        bugReportExportHandler = new BugReportExportHandler(bugReportService);
    }

    @Test
    void getEntityTypeShouldReturnBUG_REPORT() {
        assertEquals(EntityType.BUG_REPORT, bugReportExportHandler.getEntityType());
    }

    @Test
    void findByIdShouldReturnBugReportResponseFromService() {
        // given
        BugReportResponse expectedResponse = new BugReportResponse(
                1L, new ProjectInfo(1L, "Title", 2L), new UserInfo(1L,"firstName","lastName"),"title","Description", "Environment", BugReportSeverityName.LOW, BugReportStatusName.OPENED, Instant.now(), Instant.now()
        );
        when(bugReportService.getBugReportById(123L)).thenReturn(expectedResponse);

        // when
        BugReportResponse actual = bugReportExportHandler.findById(123L);

        // then
        assertNotNull(actual);
        assertSame(expectedResponse, actual);
        verify(bugReportService).getBugReportById(123L);
    }

    @Test
    void toCsvShouldReturnEmptyStringIfBugReportIsNull() {
        // when
        String csv = bugReportExportHandler.toCsv(null);

        // then
        assertEquals("", csv);
    }

    @Test
    void toCsvShouldReturnCorrectCsv() {
        // given
        // Припустимо, що BugReportResponse має вкладений ProjectResponse
        BugReportResponse expectedResponse = new BugReportResponse(
                1L, new ProjectInfo(1L, "Title", 2L), new UserInfo(1L,"firstName","lastName"),"title","Description", "Environment", BugReportSeverityName.LOW, BugReportStatusName.OPENED, Instant.now(), Instant.now()
        );
        // when
        String csv = bugReportExportHandler.toCsv(expectedResponse);

        // then
        String[] lines = csv.split("\n");
        assertEquals(2, lines.length);

        // Заголовок:
        assertEquals("ProjectId,Title,Description,Environment,Severity", lines[0]);

        // Дані:
        String data = lines[1];
        assertTrue(data.contains("1"));
        assertTrue(data.contains("title"));
        assertTrue(data.contains("Description"));
        assertTrue(data.contains("Environment"));
        assertTrue(data.contains(BugReportSeverityName.LOW.name()));
    }

    @Test
    void toXmlShouldReturnEmptyBugreportWhenNull() {
        // when
        String xml = bugReportExportHandler.toXml(null);

        // then
        assertEquals("<bugreport></bugreport>", xml);
    }

    @Test
    void toXmlShouldReturnCorrectXml() {
        // given
        BugReportResponse bugReport = new BugReportResponse(
                1L, new ProjectInfo(1L,"Title", 2L), new UserInfo(1L, "firstName", "lastName"), "UI not responsive",
                "On Android 9, the UI layout breaks...", "Android 9", BugReportSeverityName.LOW,
                BugReportStatusName.OPENED, Instant.now(), Instant.now()
        );

        // when
        String xml = bugReportExportHandler.toXml(bugReport);

        // then
        // Перевіряємо, чи XML містить потрібні поля
        assertTrue(xml.contains("<bugreport>"));
        assertTrue(xml.contains("</bugreport>"));
        assertTrue(xml.contains("<projectId>1</projectId>"));
        assertTrue(xml.contains("<title>UI not responsive</title>"));
        assertTrue(xml.contains("<description>On Android 9, the UI layout breaks...</description>"));
        assertTrue(xml.contains("<environment>Android 9</environment>"));
        assertTrue(xml.contains("<severity>LOW</severity>"));
    }
}
