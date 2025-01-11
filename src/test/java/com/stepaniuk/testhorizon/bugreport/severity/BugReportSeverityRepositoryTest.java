package com.stepaniuk.testhorizon.bugreport.severity;

import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/bugreport/bugreport_severities.sql"})
class BugReportSeverityRepositoryTest {

    @Autowired
    private BugReportSeverityRepository bugReportSeverityRepository;

    @Test
    void shouldSaveBugReportSeverity() {
        // given
        BugReportSeverity severityToSave = new BugReportSeverity(null, BugReportSeverityName.HIGH);

        // when
        BugReportSeverity savedSeverity = bugReportSeverityRepository.save(severityToSave);

        // then
        assertNotNull(savedSeverity);
        assertNotNull(savedSeverity.getId());
        assertEquals(severityToSave.getName(), savedSeverity.getName());
    }

    @Test
    void shouldThrowExceptionWhenSavingBugReportSeverityWithoutName() {
        // given
        BugReportSeverity severityToSave = new BugReportSeverity(null, null);

        // when && then
        assertThrows(DataIntegrityViolationException.class, () -> bugReportSeverityRepository.save(severityToSave));
    }

    @Test
    void shouldReturnBugReportSeverityWhenFindById() {
        // when
        Optional<BugReportSeverity> optionalSeverity = bugReportSeverityRepository.findById(1L);

        // then
        assertTrue(optionalSeverity.isPresent());
        BugReportSeverity severity = optionalSeverity.get();

        assertEquals(1L, severity.getId());
        assertEquals(BugReportSeverityName.LOW, severity.getName());
    }

    @Test
    void shouldReturnBugReportSeverityWhenFindByName() {
        // when
        Optional<BugReportSeverity> optionalSeverity = bugReportSeverityRepository.findByName(BugReportSeverityName.LOW);

        // then
        assertTrue(optionalSeverity.isPresent());
        BugReportSeverity severity = optionalSeverity.get();

        assertEquals(1L, severity.getId());
        assertEquals(BugReportSeverityName.LOW, severity.getName());
    }

    @Test
    void shouldUpdateBugReportSeverityWhenChangingName() {
        // given
        BugReportSeverity severityToUpdate = bugReportSeverityRepository.findById(1L).orElseThrow();
        severityToUpdate.setName(BugReportSeverityName.CRITICAL);

        // when
        BugReportSeverity updatedSeverity = bugReportSeverityRepository.save(severityToUpdate);

        // then
        assertEquals(severityToUpdate.getId(), updatedSeverity.getId());
        assertEquals(severityToUpdate.getName(), updatedSeverity.getName());
    }

    @Test
    void shouldDeleteBugReportSeverityWhenDeletingByExistingSeverity() {
        // given
        BugReportSeverity severityToDelete = bugReportSeverityRepository.findById(1L).orElseThrow();

        // when
        bugReportSeverityRepository.delete(severityToDelete);

        // then
        assertTrue(bugReportSeverityRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeleteBugReportSeverityByIdWhenDeletingByExistingId() {
        // when
        bugReportSeverityRepository.deleteById(1L);

        // then
        assertTrue(bugReportSeverityRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenBugReportSeverityExists() {
        // when
        boolean exists = bugReportSeverityRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenBugReportSeverityDoesNotExist() {
        // when
        boolean exists = bugReportSeverityRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnAllBugReportSeverities() {
        // when
        List<BugReportSeverity> severities = bugReportSeverityRepository.findAll();

        // then
        assertNotNull(severities);
        assertFalse(severities.isEmpty());
    }
}
