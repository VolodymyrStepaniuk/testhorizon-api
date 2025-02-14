package com.stepaniuk.testhorizon.project.status;

import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import com.stepaniuk.testhorizon.types.project.ProjectStatusName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/project/project_statuses.sql"})
class ProjectStatusRepositoryTest {

    @Autowired
    private ProjectStatusRepository projectStatusRepository;

    @Test
    void shouldSaveProjectStatus() {
        // given
        ProjectStatus projectStatus = new ProjectStatus(null, ProjectStatusName.ACTIVE);

        // when
        ProjectStatus savedProjectStatus = projectStatusRepository.save(projectStatus);

        // then
        assertNotNull(savedProjectStatus);
        assertNotNull(savedProjectStatus.getId());
        assertEquals(projectStatus.getName(), savedProjectStatus.getName());
    }

    @Test
    void shouldThrowExceptionWhenSavingProjectStatusWithNullName() {
        // given
        ProjectStatus projectStatus = new ProjectStatus(null, null);

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> projectStatusRepository.save(projectStatus));
    }

    @Test
    void shouldReturnProjectStatusWhenFindById() {
        // when
        ProjectStatus projectStatus = projectStatusRepository.findById(1L).orElseThrow();

        // then
        assertNotNull(projectStatus);
        assertEquals(1L, projectStatus.getId());
        assertEquals(ProjectStatusName.ACTIVE, projectStatus.getName());
    }

    @Test
    void shouldReturnProjectStatusWhenFindByName() {
        // when
        ProjectStatus projectStatus = projectStatusRepository.findByName(ProjectStatusName.ACTIVE).orElseThrow();

        // then
        assertNotNull(projectStatus);
        assertEquals(1L, projectStatus.getId());
        assertEquals(ProjectStatusName.ACTIVE, projectStatus.getName());
    }

    @Test
    void shouldUpdateProjectStatusWhenChangingName() {
        // given
        ProjectStatus projectStatus = projectStatusRepository.findById(1L).orElseThrow();
        projectStatus.setName(ProjectStatusName.INACTIVE);

        // when
        ProjectStatus updatedProjectStatus = projectStatusRepository.save(projectStatus);

        // then
        assertNotNull(updatedProjectStatus);
        assertEquals(projectStatus.getId(), updatedProjectStatus.getId());
        assertEquals(ProjectStatusName.INACTIVE, updatedProjectStatus.getName());
    }

    @Test
    void shouldDeleteProjectStatusWhenDeletingByExistingProjectStatus() {
        // given
        ProjectStatus projectStatus = projectStatusRepository.findById(1L).orElseThrow();

        // when
        projectStatusRepository.delete(projectStatus);

        // then
        assertFalse(projectStatusRepository.findById(1L).isPresent());
    }

    @Test
    void shouldDeleteProjectStatusWhenDeletingByExistingProjectStatusId() {
        // when
        projectStatusRepository.deleteById(1L);

        // then
        assertFalse(projectStatusRepository.findById(1L).isPresent());
    }

    @Test
    void shouldReturnTrueWhenExistsByExistingProjectStatusId() {
        // when
        boolean exists = projectStatusRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenExistsByNonExistingProjectStatusId() {
        // when
        boolean exists = projectStatusRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNotEmptyListWhenFindAll() {
        // when
        List<ProjectStatus> projectStatuses = projectStatusRepository.findAll();

        // then
        assertNotNull(projectStatuses);
        assertFalse(projectStatuses.isEmpty());
    }
}
