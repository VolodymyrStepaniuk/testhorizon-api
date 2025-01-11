package com.stepaniuk.testhorizon.bugreport.status;

import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/bugreport/bugreport_statuses.sql"})
class BugReportStatusRepositoryTest {

    @Autowired
    private BugReportStatusRepository bugReportStatusRepository;

    @Test
    void shouldSaveBugReportStatus() {
        // given
        BugReportStatus statusToSave = new BugReportStatus(null, BugReportStatusName.OPENED);

        // when
        BugReportStatus savedStatus = bugReportStatusRepository.save(statusToSave);

        // then
        assertNotNull(savedStatus);
        assertNotNull(savedStatus.getId());
        assertEquals(statusToSave.getName(), savedStatus.getName());
    }

    @Test
    void shouldThrowExceptionWhenSavingBugReportStatusWithoutName() {
        // given
        BugReportStatus statusToSave = new BugReportStatus(null, null);

        // when && then
        assertThrows(DataIntegrityViolationException.class, () -> bugReportStatusRepository.save(statusToSave));
    }

    @Test
    void shouldReturnBugReportStatusWhenFindById() {
        // when
        Optional<BugReportStatus> optionalStatus = bugReportStatusRepository.findById(1L);

        // then
        assertTrue(optionalStatus.isPresent());
        BugReportStatus status = optionalStatus.get();

        assertEquals(1L, status.getId());
        assertEquals(BugReportStatusName.OPENED, status.getName());
    }

    @Test
    void shouldReturnBugReportStatusWhenFindByName() {
        // when
        Optional<BugReportStatus> optionalStatus = bugReportStatusRepository.findByName(BugReportStatusName.OPENED);

        // then
        assertTrue(optionalStatus.isPresent());
        BugReportStatus status = optionalStatus.get();

        assertEquals(1L, status.getId());
        assertEquals(BugReportStatusName.OPENED, status.getName());
    }

    @Test
    void shouldUpdateBugReportStatusWhenChangingName() {
        // given
        BugReportStatus statusToUpdate = bugReportStatusRepository.findById(1L).orElseThrow();
        statusToUpdate.setName(BugReportStatusName.CLOSED);

        // when
        BugReportStatus updatedStatus = bugReportStatusRepository.save(statusToUpdate);

        // then
        assertEquals(statusToUpdate.getId(), updatedStatus.getId());
        assertEquals(statusToUpdate.getName(), updatedStatus.getName());
    }

    @Test
    void shouldDeleteBugReportStatusWhenDeletingByExistingStatus() {
        // given
        BugReportStatus statusToDelete = bugReportStatusRepository.findById(1L).orElseThrow();

        // when
        bugReportStatusRepository.delete(statusToDelete);

        // then
        assertTrue(bugReportStatusRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeleteBugReportStatusByIdWhenDeletingByExistingId() {
        // when
        bugReportStatusRepository.deleteById(1L);

        // then
        assertTrue(bugReportStatusRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenBugReportStatusExists() {
        // when
        boolean exists = bugReportStatusRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenBugReportStatusDoesNotExist() {
        // when
        boolean exists = bugReportStatusRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnAllBugReportStatuses() {
        // when
        List<BugReportStatus> statuses = bugReportStatusRepository.findAll();

        // then
        assertNotNull(statuses);
        assertFalse(statuses.isEmpty());
    }
}
