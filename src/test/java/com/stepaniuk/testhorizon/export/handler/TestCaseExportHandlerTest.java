package com.stepaniuk.testhorizon.export.handler;

import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseResponse;
import com.stepaniuk.testhorizon.testcase.TestCaseService;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import com.stepaniuk.testhorizon.types.testcase.TestCasePriorityName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestCaseExportHandlerTest {

    @Mock
    private TestCaseService testCaseService;

    private TestCaseExportHandler testCaseExportHandler;

    @BeforeEach
    void setUp() {
        // Створюємо handler, передаючи мок-сервіс
        testCaseExportHandler = new TestCaseExportHandler(testCaseService);
    }

    @Test
    void getEntityTypeShouldReturnTEST_CASE() {
        // Перевірка, що getEntityType() повертає TEST_CASE
        assertEquals(EntityType.TEST_CASE, testCaseExportHandler.getEntityType());
    }

    @Test
    void findByIdShouldReturnTestCaseResponseFromService() {
        // given
        var testCaseResponse = new TestCaseResponse(
                1L, new ProjectInfo(1L, "Title", 2L), new UserInfo(1L, "firstName", "lastName"), "MyTitle", "Desc",
                "preconditions", "inputData", List.of("Step 1", "Step 2"), TestCasePriorityName.HIGH, Instant.now(), Instant.now()
        );
        when(testCaseService.getTestCaseById(10L)).thenReturn(testCaseResponse);

        // when
        var actual = testCaseExportHandler.findById(10L);

        // then
        assertNotNull(actual);
        assertSame(testCaseResponse, actual); // Переконуємося, що це той самий об'єкт
        verify(testCaseService).getTestCaseById(10L);
    }

    @Test
    void toCsvShouldReturnEmptyStringIfNull() {
        // when
        String result = testCaseExportHandler.toCsv(null);

        // then
        assertEquals("", result);
    }

    @Test
    void toCsvShouldReturnHeaderAndCorrectData() {
        // given
        var testCaseResponse = new TestCaseResponse(
                1L, new ProjectInfo(1L, "Title", 2L), new UserInfo(1L, "firstName", "lastName"), "MyTitle", "Desc",
                "preconditions", "inputData", List.of("Step 1", "Step 2"), TestCasePriorityName.HIGH, Instant.now(), Instant.now()
        );

        // when
        String csv = testCaseExportHandler.toCsv(testCaseResponse);

        // then
        String[] lines = csv.split("\n");
        assertEquals(2, lines.length);

        assertEquals("ProjectId,Title,Description,Preconditions,InputData,Steps,Priority", lines[0]);

        String dataLine = lines[1];
        assertTrue(dataLine.contains("1"));
        assertTrue(dataLine.contains("MyTitle"));
        assertTrue(dataLine.contains("Desc"));
        assertTrue(dataLine.contains("preconditions"));
        assertTrue(dataLine.contains("inputData"));
        assertTrue(dataLine.contains("Step 1;Step 2"));
        assertTrue(dataLine.contains("HIGH"));
    }

    @Test
    void toXmlShouldReturnEmptyTestcaseWhenNull() {
        // when
        String xml = testCaseExportHandler.toXml(null);

        // then
        assertEquals("<testcase></testcase>", xml);
    }

    @Test
    void toXmlShouldReturnCorrectXml() {
        // given
        var testCaseResponse = new TestCaseResponse(
                1L, new ProjectInfo(1L, "Title", 2L), new UserInfo(1L, "firstName", "lastName"), "MyTitle", "Desc",
                "preconditions", "inputData", List.of("Step 1", "Step 2"), TestCasePriorityName.HIGH, Instant.now(), Instant.now()
        );

        // when
        String xml = testCaseExportHandler.toXml(testCaseResponse);

        // then
        assertTrue(xml.contains("<testcase>"));
        assertTrue(xml.contains("</testcase>"));
        assertTrue(xml.contains("<projectId>1</projectId>"));
        assertTrue(xml.contains("<title>MyTitle</title>"));
        assertTrue(xml.contains("<description>Desc</description>"));
        assertTrue(xml.contains("<preconditions>preconditions</preconditions>"));
        assertTrue(xml.contains("<inputData>inputData</inputData>"));
        assertTrue(xml.contains("<steps>"));
        assertTrue(xml.contains("<step>Step 1</step>"));
        assertTrue(xml.contains("<step>Step 2</step>"));
        assertTrue(xml.contains("<priority>HIGH</priority>"));
    }
}