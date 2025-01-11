package com.stepaniuk.testhorizon.testcase;

import com.stepaniuk.testhorizon.payload.testcase.TestCaseResponse;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriority;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityName;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MapperLevelUnitTest
@ContextConfiguration(classes = {TestCaseMapperImpl.class})
class TestCaseMapperTest {

    @Autowired
    private TestCaseMapper testCaseMapper;

    @Test
    void shouldMapTestCaseToTestCaseResponse() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        TestCasePriority testCasePriority = new TestCasePriority(1L, TestCasePriorityName.HIGH);

        TestCase testCaseToMap = new TestCase(null, 1L, 1L, "TestCase Title", "Description 4",
                "Preconditions 4", "Input Data 4", List.of("Step 1", "Step 2"), testCasePriority, timeOfCreation, timeOfModification);

        // when
        TestCaseResponse testCaseResponse = testCaseMapper.toResponse(testCaseToMap);

        // then
        assertNotNull(testCaseResponse);
        assertNull(testCaseResponse.getId());
        assertEquals(testCaseToMap.getProjectId(), testCaseResponse.getProjectId());
        assertEquals(testCaseToMap.getAuthorId(), testCaseResponse.getAuthorId());
        assertEquals(testCaseToMap.getTitle(), testCaseResponse.getTitle());
        assertEquals(testCaseToMap.getDescription(), testCaseResponse.getDescription());
        assertEquals(testCaseToMap.getPreconditions(), testCaseResponse.getPreconditions());
        assertEquals(testCaseToMap.getInputData(), testCaseResponse.getInputData());
        assertEquals(testCaseToMap.getSteps(), testCaseResponse.getSteps());
        assertEquals(testCaseToMap.getPriority().getName(), testCaseResponse.getPriority());
        assertEquals(testCaseToMap.getCreatedAt(), testCaseResponse.getCreatedAt());
        assertEquals(testCaseToMap.getUpdatedAt(), testCaseResponse.getUpdatedAt());
        assertTrue(testCaseResponse.hasLinks());
        assertTrue(testCaseResponse.getLinks().hasLink("self"));
        assertTrue(testCaseResponse.getLinks().hasLink("update"));
        assertTrue(testCaseResponse.getLinks().hasLink("delete"));
    }
}
