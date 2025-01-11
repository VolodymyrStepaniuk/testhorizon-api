package com.stepaniuk.testhorizon.project;

import com.stepaniuk.testhorizon.payload.project.ProjectResponse;
import com.stepaniuk.testhorizon.project.status.ProjectStatus;
import com.stepaniuk.testhorizon.project.status.ProjectStatusName;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MapperLevelUnitTest
@ContextConfiguration(classes = {ProjectMapperImpl.class})
class ProjectMapperTest {

    @Autowired
    private ProjectMapper projectMapper;

    @Test
    void shouldMapProjectToProjectResponse() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        ProjectStatus projectStatus = new ProjectStatus(1L, ProjectStatusName.ACTIVE);

        Project project = new Project(null, 1L, "Project title", "Project description",
                "Project instructions", "github.com/user/repo", List.of("image1", "image2"), projectStatus, timeOfCreation, timeOfModification);

        // when
        ProjectResponse projectResponse = projectMapper.toResponse(project);

        // then
        assertNotNull(projectResponse);
        assertNull(projectResponse.getId());
        assertEquals(project.getOwnerId(), projectResponse.getOwnerId());
        assertEquals(project.getTitle(), projectResponse.getTitle());
        assertEquals(project.getDescription(), projectResponse.getDescription());
        assertEquals(project.getInstructions(), projectResponse.getInstructions());
        assertEquals(project.getGithubUrl(), projectResponse.getGithubUrl());
        assertEquals(project.getImageUrls(), projectResponse.getImageUrls());
        assertEquals(project.getStatus().getName(), projectResponse.getStatus());
        assertEquals(project.getCreatedAt(), projectResponse.getCreatedAt());
        assertEquals(project.getUpdatedAt(), projectResponse.getUpdatedAt());
        assertTrue(projectResponse.hasLinks());
        assertTrue(projectResponse.getLinks().hasLink("self"));
        assertTrue(projectResponse.getLinks().hasLink("update"));
        assertTrue(projectResponse.getLinks().hasLink("delete"));
    }
}
