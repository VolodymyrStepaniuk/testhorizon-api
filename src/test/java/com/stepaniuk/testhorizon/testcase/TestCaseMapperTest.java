package com.stepaniuk.testhorizon.testcase;

import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.testcase.TestCaseResponse;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriority;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import com.stepaniuk.testhorizon.types.testcase.TestCasePriorityName;
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
        var userInfo = new UserInfo(testCaseToMap.getAuthorId(), "User Name", "User Surname");
        var projectInfo = new ProjectInfo(testCaseToMap.getProjectId(), "Project Title");

        // when
        TestCaseResponse testCaseResponse = testCaseMapper.toResponse(testCaseToMap, projectInfo, userInfo);

        // then
        assertNotNull(testCaseResponse);
        assertNull(testCaseResponse.getId());
        assertNotNull(testCaseResponse.getProject());
        assertEquals(testCaseToMap.getProjectId(), testCaseResponse.getProject().getId());
        assertEquals(projectInfo.getTitle(), testCaseResponse.getProject().getTitle());
        assertNotNull(testCaseResponse.getAuthor());
        assertEquals(testCaseToMap.getAuthorId(), testCaseResponse.getAuthor().getId());
        assertEquals(userInfo.getFirstName(), testCaseResponse.getAuthor().getFirstName());
        assertEquals(userInfo.getLastName(), testCaseResponse.getAuthor().getLastName());
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
