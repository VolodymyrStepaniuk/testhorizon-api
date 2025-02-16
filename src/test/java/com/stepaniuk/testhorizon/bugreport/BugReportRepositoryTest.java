package com.stepaniuk.testhorizon.bugreport;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverity;
import com.stepaniuk.testhorizon.types.bugreport.BugReportSeverityName;
import com.stepaniuk.testhorizon.bugreport.status.BugReportStatus;
import com.stepaniuk.testhorizon.types.bugreport.BugReportStatusName;
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
@Sql(scripts = {"classpath:sql/bugreport/bugreport_severities.sql", "classpath:sql/bugreport/bugreport_statuses.sql", "classpath:sql/bugreport/bugreports.sql"})
class BugReportRepositoryTest {

    @Autowired
    private BugReportRepository bugReportRepository;

    @Test
    void shouldSaveBugReport() {
        // given
        BugReportSeverity severity = new BugReportSeverity(1L, BugReportSeverityName.HIGH);
        BugReportStatus status = new BugReportStatus(1L, BugReportStatusName.OPENED);
        BugReport bugReportToSave = new BugReport(null, 1L, "Bug Report Title", "Description",
                "Enviroment",1L,severity, status, Instant.now(), Instant.now());

        // when
        BugReport savedBugReport = bugReportRepository.save(bugReportToSave);

        // then
        assertNotNull(savedBugReport);
        assertNotNull(savedBugReport.getId());
        assertEquals(bugReportToSave.getProjectId(), savedBugReport.getProjectId());
        assertEquals(bugReportToSave.getTitle(), savedBugReport.getTitle());
        assertEquals(bugReportToSave.getDescription(), savedBugReport.getDescription());
        assertEquals(bugReportToSave.getEnvironment(), savedBugReport.getEnvironment());
        assertEquals(bugReportToSave.getReporterId(), savedBugReport.getReporterId());
        assertEquals(bugReportToSave.getSeverity(), savedBugReport.getSeverity());
        assertEquals(bugReportToSave.getStatus(), savedBugReport.getStatus());
        assertEquals(bugReportToSave.getCreatedAt(), savedBugReport.getCreatedAt());
        assertEquals(bugReportToSave.getUpdatedAt(), savedBugReport.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingBugReportWithoutTitle() {
        // given
        BugReportSeverity severity = new BugReportSeverity(1L, BugReportSeverityName.HIGH);
        BugReportStatus status = new BugReportStatus(1L, BugReportStatusName.OPENED);
        BugReport bugReportToSave = new BugReport(null, 1L, null, "Description",
                "Enviroment",1L,  severity, status, Instant.now(), Instant.now());

        // when && then
        assertThrows(DataIntegrityViolationException.class, () -> bugReportRepository.save(bugReportToSave));
    }

    @Test
    void shouldReturnBugReportWhenFindById() {
        // when
        Optional<BugReport> optionalBugReport = bugReportRepository.findById(1L);

        // then
        assertTrue(optionalBugReport.isPresent());
        BugReport bugReport = optionalBugReport.get();

        assertEquals(1L, bugReport.getId());
        assertEquals("Bug report title", bugReport.getTitle());
        assertEquals("Bug report description", bugReport.getDescription());
        assertEquals("Bug report environment", bugReport.getEnvironment());
        assertEquals(1L, bugReport.getReporterId());
        assertEquals(BugReportSeverityName.LOW, bugReport.getSeverity().getName());
        assertEquals(BugReportStatusName.OPENED, bugReport.getStatus().getName());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), bugReport.getCreatedAt());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), bugReport.getUpdatedAt());
    }

    @Test
    void shouldUpdateBugReportWhenChangingTitle() {
        // given
        BugReport bugReportToUpdate = bugReportRepository.findById(1L).orElseThrow();
        bugReportToUpdate.setTitle("Updated Bug Report Title");

        // when
        BugReport updatedBugReport = bugReportRepository.save(bugReportToUpdate);

        // then
        assertEquals(bugReportToUpdate.getId(), updatedBugReport.getId());
        assertEquals(bugReportToUpdate.getTitle(), updatedBugReport.getTitle());
    }

    @Test
    void shouldDeleteBugReportWhenDeletingByExistingBugReport() {
        // given
        BugReport bugReportToDelete = bugReportRepository.findById(1L).orElseThrow();

        // when
        bugReportRepository.delete(bugReportToDelete);

        // then
        assertTrue(bugReportRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeleteBugReportByIdWhenDeletingByExistingId() {
        // when
        bugReportRepository.deleteById(1L);

        // then
        assertTrue(bugReportRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenBugReportExists() {
        // when
        boolean exists = bugReportRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenBugReportDoesNotExist() {
        // when
        boolean exists = bugReportRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<BugReport> bugReports = bugReportRepository.findAll();

        // then
        assertNotNull(bugReports);
        assertFalse(bugReports.isEmpty());
    }
}
