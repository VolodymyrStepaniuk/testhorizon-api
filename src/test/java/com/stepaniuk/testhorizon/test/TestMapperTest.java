package com.stepaniuk.testhorizon.test;

import com.stepaniuk.testhorizon.payload.info.ProjectInfo;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.test.TestResponse;
import com.stepaniuk.testhorizon.test.type.TestType;
import com.stepaniuk.testhorizon.types.test.TestTypeName;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@MapperLevelUnitTest
@ContextConfiguration(classes = {TestMapperImpl.class})
class TestMapperTest {

    @Autowired
    private TestMapper testMapper;

    @org.junit.jupiter.api.Test
    void shouldMapTestToTestResponse() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        TestType testType = new TestType(1L, TestTypeName.FUNCTIONAL);

        Test test = new Test(null, 1L, 1L, 1L, "Test title", "Test description",
                "Test instructions", "https://github.com/user/repo", testType, timeOfCreation, timeOfModification);
        var projectInfo = new ProjectInfo(1L, "Project title");
        var authorInfo = new UserInfo(1L, "Author name","Author surname");

        // when
        TestResponse testResponse = testMapper.toResponse(test, projectInfo, authorInfo);

        // then
        assertNotNull(testResponse);
        assertNull(testResponse.getId());
        assertNotNull(testResponse.getProject());
        assertEquals(test.getProjectId(), testResponse.getProject().getId());
        assertEquals(projectInfo.getTitle(), testResponse.getProject().getTitle());
        assertEquals(test.getTestCaseId(), testResponse.getTestCaseId());
        assertNotNull(testResponse.getAuthor());
        assertEquals(test.getAuthorId(), testResponse.getAuthor().getId());
        assertEquals(authorInfo.getFirstName(), testResponse.getAuthor().getFirstName());
        assertEquals(authorInfo.getLastName(), testResponse.getAuthor().getLastName());
        assertEquals(test.getTitle(), testResponse.getTitle());
        assertEquals(test.getDescription(), testResponse.getDescription());
        assertEquals(test.getInstructions(), testResponse.getInstructions());
        assertEquals(test.getGithubUrl(), testResponse.getGithubUrl());
        assertEquals(test.getType().getName(), testResponse.getType());
        assertEquals(test.getCreatedAt(), testResponse.getCreatedAt());
        assertEquals(test.getUpdatedAt(), testResponse.getUpdatedAt());
        assertTrue(testResponse.hasLinks());
        assertTrue(testResponse.getLinks().hasLink("self"));
        assertTrue(testResponse.getLinks().hasLink("update"));
        assertTrue(testResponse.getLinks().hasLink("delete"));
    }
}
