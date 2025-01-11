package com.stepaniuk.testhorizon.testcase;

import com.stepaniuk.testhorizon.testcase.priority.TestCasePriority;
import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityName;
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
@Sql(scripts = {"classpath:sql/testcase/test_case_priorities.sql","classpath:sql/testcase/testcases.sql"})
class TestCaseRepositoryTest {
    @Autowired
    private TestCaseRepository testCaseRepository;

    @Test
    void shouldSaveTestCase() {
        // given
        TestCasePriority testCasePriority = new TestCasePriority(1L, TestCasePriorityName.HIGH);
        TestCase testCaseToSave = new TestCase(null, 1L, 1L, "TestCase Title", "Description 4", "Preconditions 4", "Input Data 4", List.of("Step 1", "Step 2"), testCasePriority, Instant.now(), Instant.now());

        // when
        TestCase savedTestCase = testCaseRepository.save(testCaseToSave);

        // then
        assertNotNull(savedTestCase);
        assertNotNull(savedTestCase.getId());
        assertEquals(testCaseToSave.getProjectId(), savedTestCase.getProjectId());
        assertEquals(testCaseToSave.getAuthorId(), savedTestCase.getAuthorId());
        assertEquals(testCaseToSave.getTitle(), savedTestCase.getTitle());
        assertEquals(testCaseToSave.getDescription(), savedTestCase.getDescription());
        assertEquals(testCaseToSave.getPreconditions(), savedTestCase.getPreconditions());
        assertEquals(testCaseToSave.getInputData(), savedTestCase.getInputData());
        assertEquals(testCaseToSave.getSteps(), savedTestCase.getSteps());
        assertEquals(testCaseToSave.getPriority(), savedTestCase.getPriority());
        assertEquals(testCaseToSave.getCreatedAt(), savedTestCase.getCreatedAt());
        assertEquals(testCaseToSave.getUpdatedAt(), savedTestCase.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingTestCaseWithoutName() {
        // given
        TestCase testCaseToSave = new TestCase(null, 1L, 1L, null, "Description 4", "Preconditions 4", "Input Data 4", List.of("Step 1", "Step 2"), null, Instant.now(), Instant.now());

        // when && then
        assertThrows(DataIntegrityViolationException.class, () -> testCaseRepository.save(testCaseToSave));
    }

    @Test
    void shouldReturnTestCaseWhenFindById() {
        // when
        Optional<TestCase> optionalTestCase = testCaseRepository.findById(1L);

        // then
        assertTrue(optionalTestCase.isPresent());
        TestCase testCase = optionalTestCase.get();

        assertEquals(1L, testCase.getId());
        assertEquals(1L, testCase.getProjectId());
        assertEquals(1L, testCase.getAuthorId());
        assertEquals("Test case title", testCase.getTitle());
        assertEquals("Test case description", testCase.getDescription());
        assertEquals("Test case preconditions", testCase.getPreconditions());
        assertEquals("Test case input data", testCase.getInputData());
        assertEquals(List.of("Step 1", "Step 2"), testCase.getSteps());
        assertEquals(TestCasePriorityName.LOW, testCase.getPriority().getName());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), testCase.getCreatedAt());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), testCase.getUpdatedAt());
    }


    @Test
    void shouldUpdateTestCaseWhenChangingTitle() {
        // given
        TestCase testCaseToUpdate = testCaseRepository.findById(1L).orElseThrow();
        testCaseToUpdate.setTitle("Updated Test Case 1");

        // when
        TestCase updatedTestCase = testCaseRepository.save(testCaseToUpdate);

        // then
        assertEquals(testCaseToUpdate.getId(), updatedTestCase.getId());
        assertEquals("Updated Test Case 1", updatedTestCase.getTitle());
    }

    @Test
    void shouldDeleteTestCaseWhenDeletingByExistingTestCase() {
        // given
        TestCase testCaseToDelete = testCaseRepository.findById(1L).orElseThrow();

        // when
        testCaseRepository.delete(testCaseToDelete);

        // then
        assertTrue(testCaseRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeleteTestCaseByIdWhenDeletingByExistingId() {
        // when
        testCaseRepository.deleteById(1L);

        // then
        assertTrue(testCaseRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenTestCaseExists() {
        // when
        boolean exists = testCaseRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenTestCaseDoesNotExist() {
        // when
        boolean exists = testCaseRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<TestCase> testCases = testCaseRepository.findAll();

        // then
        assertNotNull(testCases);
        assertFalse(testCases.isEmpty());
    }
}
