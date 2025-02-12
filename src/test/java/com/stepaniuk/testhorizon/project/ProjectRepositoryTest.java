package com.stepaniuk.testhorizon.project;

import com.stepaniuk.testhorizon.project.status.ProjectStatus;
import com.stepaniuk.testhorizon.project.status.ProjectStatusName;
import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/project/project_statuses.sql", "classpath:sql/project/projects.sql"})
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldSaveProject() {
        // given
        ProjectStatus projectStatus = new ProjectStatus(1L, ProjectStatusName.ACTIVE);
        Project projectToSave = new Project(null, 1L, "New Project", "Project description",
                "Project description","github.com",projectStatus, Instant.now(), Instant.now());

        // when
        Project savedProject = projectRepository.save(projectToSave);

        // then
        assertNotNull(savedProject);
        assertNotNull(savedProject.getId());
        assertEquals(projectToSave.getOwnerId(), savedProject.getOwnerId());
        assertEquals(projectToSave.getTitle(), savedProject.getTitle());
        assertEquals(projectToSave.getDescription(), savedProject.getDescription());
        assertEquals(projectToSave.getInstructions(), savedProject.getInstructions());
        assertEquals(projectToSave.getGithubUrl(), savedProject.getGithubUrl());
        assertEquals(projectToSave.getStatus(), savedProject.getStatus());
        assertEquals(projectToSave.getCreatedAt(), savedProject.getCreatedAt());
        assertEquals(projectToSave.getUpdatedAt(), savedProject.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingProjectWithoutTitle() {
        // given
        ProjectStatus projectStatus = new ProjectStatus(1L, ProjectStatusName.ACTIVE);
        Project projectToSave = new Project(null, 1L, null, "Project description",
                "Project description","github.com",projectStatus, Instant.now(), Instant.now());

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> projectRepository.save(projectToSave));
    }

    @Test
    void shouldReturnProjectWhenFindById() {
        // when
        Optional<Project> optionalProject = projectRepository.findById(1L);

        // then
        assertTrue(optionalProject.isPresent());
        Project project = optionalProject.get();

        assertEquals(1L, project.getId());
        assertEquals(1L, project.getOwnerId());
        assertEquals("Project title", project.getTitle());
        assertEquals("Project description", project.getDescription());
        assertEquals("Instructions", project.getInstructions());
        assertEquals("github.com", project.getGithubUrl());
        assertEquals(ProjectStatusName.ACTIVE, project.getStatus().getName());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), project.getCreatedAt());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), project.getUpdatedAt());
    }

    @Test
    void shouldReturnOwnerIdWhenFindById() {
        // when
        Optional<Project> optionalProject = projectRepository.findById(1L);

        // then
        assertTrue(optionalProject.isPresent());
        Project project = optionalProject.get();

        assertEquals(1L, project.getOwnerId());
    }

    @Test
    void shouldUpdateProjectWhenChangingTitle() {
        // given
        Project projectToUpdate = projectRepository.findById(1L).orElseThrow();
        projectToUpdate.setTitle("Updated Project Title");

        // when
        Project updatedProject = projectRepository.save(projectToUpdate);

        // then
        assertEquals(projectToUpdate.getId(), updatedProject.getId());
        assertEquals("Updated Project Title", updatedProject.getTitle());
    }

    @Test
    void shouldDeleteProjectWhenDeletingByExistingProject() {
        // given
        Project projectToDelete = projectRepository.findById(1L).orElseThrow();

        // when
        projectRepository.delete(projectToDelete);

        // then
        assertTrue(projectRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeleteProjectByIdWhenDeletingByExistingId() {
        // when
        projectRepository.deleteById(1L);

        // then
        assertTrue(projectRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenProjectExists() {
        // when
        boolean exists = projectRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenProjectDoesNotExist() {
        // when
        boolean exists = projectRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<Project> projects = projectRepository.findAll();

        // then
        assertNotNull(projects);
        assertFalse(projects.isEmpty());
    }
}
