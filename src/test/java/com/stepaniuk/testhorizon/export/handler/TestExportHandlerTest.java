package com.stepaniuk.testhorizon.export.handler;

import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.test.TestResponse;
import com.stepaniuk.testhorizon.test.TestService;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import com.stepaniuk.testhorizon.types.test.TestTypeName;
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
class TestExportHandlerTest {

    @Mock
    private TestService testService;

    private TestExportHandler testExportHandler;

    @BeforeEach
    void setUp() {
        testExportHandler = new TestExportHandler(testService);
    }

    @Test
    void getEntityTypeShouldReturnTEST() {
        assertEquals(EntityType.TEST, testExportHandler.getEntityType());
    }

    @Test
    void findByIdShouldReturnTestResponseFromService() {
        // given
        var testResponse = new TestResponse(
                111L, new ProjectInfo(1L, "Title"), 1L, new UserInfo(1L,"firstName", "lastName"),"Demo test", "Just a test", "Run instructions",
                "https://github.com/demo", TestTypeName.ACCEPTANCE, Instant.now(), Instant.now()
        );
        when(testService.getTestById(10L)).thenReturn(testResponse);

        // when
        var actual = testExportHandler.findById(10L);

        // then
        assertNotNull(actual);
        assertSame(testResponse, actual);
        verify(testService).getTestById(10L);
    }

    @Test
    void toCsvShouldReturnEmptyIfNull() {
        // when
        String csv = testExportHandler.toCsv(null);

        // then
        assertEquals("", csv);
    }

    @Test
    void toCsvShouldReturnHeaderAndData() {
        // given
        var testResponse = new TestResponse(
                111L, new ProjectInfo(1L, "Title"), 1L, new UserInfo(1L,"firstName", "lastName"),"Demo test", "Just a test", "Run instructions",
                "https://github.com/demo", TestTypeName.ACCEPTANCE, Instant.now(), Instant.now()
        );
        // when
        String csv = testExportHandler.toCsv(testResponse);

        // then
        String[] lines = csv.split("\n");
        assertEquals(2, lines.length);

        assertEquals("ProjectId,TestCaseId,Title,Description,Instructions,GithubUrl,Type", lines[0]);

        String dataRow = lines[1];
        assertTrue(dataRow.contains("1"));
        assertTrue(dataRow.contains("Demo test"));
        assertTrue(dataRow.contains("Just a test"));
        assertTrue(dataRow.contains("Run instructions"));
        assertTrue(dataRow.contains("https://github.com/demo"));
        assertTrue(dataRow.contains("ACCEPTANCE"));
    }

    @Test
    void toXmlShouldReturnEmptyIfNull() {
        // when
        String xml = testExportHandler.toXml(null);

        // then
        assertEquals("<test></test>", xml);
    }

    @Test
    void toXmlShouldReturnCorrectXml() {
        // given
        var testResponse = new TestResponse(
                111L, new ProjectInfo(1L, "Title"), 1L, new UserInfo(1L,"firstName", "lastName"),"Demo test", "Just a test", "Run instructions",
                "https://github.com/demo", TestTypeName.ACCEPTANCE, Instant.now(), Instant.now()
        );

        // when
        String xml = testExportHandler.toXml(testResponse);

        // then
        assertTrue(xml.contains("<test>"));
        assertTrue(xml.contains("</test>"));
        assertTrue(xml.contains("<projectId>1</projectId>"));
        assertTrue(xml.contains("<testCaseId>1</testCaseId>"));
        assertTrue(xml.contains("<title>Demo test</title>"));
        assertTrue(xml.contains("<description>Just a test</description>"));
        assertTrue(xml.contains("<instructions>Run instructions</instructions>"));
        assertTrue(xml.contains("<githubUrl>https://github.com/demo</githubUrl>"));
        assertTrue(xml.contains("<type>ACCEPTANCE</type>"));
    }
}