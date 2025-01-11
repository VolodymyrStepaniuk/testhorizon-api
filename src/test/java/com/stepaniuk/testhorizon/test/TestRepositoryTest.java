package com.stepaniuk.testhorizon.test;

import com.stepaniuk.testhorizon.test.type.TestType;
import com.stepaniuk.testhorizon.test.type.TestTypeName;
import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/test/test_types.sql", "classpath:sql/test/tests.sql"})
class TestRepositoryTest {

    @Autowired
    private TestRepository testRepository;

    @org.junit.jupiter.api.Test
    void shouldSaveTest() {
        // given
        TestType testType = new TestType(1L, TestTypeName.FUNCTIONAL);
        Test testToSave = new Test(null, 1L, 1L, 1L, "Test title", "Test description",
                "Test instructions", "https://github.com/user/repo", testType, Instant.now(), Instant.now());

        // when
        Test savedTest = testRepository.save(testToSave);

        // then
        assertNotNull(savedTest);
        assertNotNull(savedTest.getId());
        assertEquals(testToSave.getProjectId(), savedTest.getProjectId());
        assertEquals(testToSave.getTestCaseId(), savedTest.getTestCaseId());
        assertEquals(testToSave.getAuthorId(), savedTest.getAuthorId());
        assertEquals(testToSave.getTitle(), savedTest.getTitle());
        assertEquals(testToSave.getDescription(), savedTest.getDescription());
        assertEquals(testToSave.getInstructions(), savedTest.getInstructions());
        assertEquals(testToSave.getGithubUrl(), savedTest.getGithubUrl());
        assertEquals(testToSave.getType(), savedTest.getType());
        assertEquals(testToSave.getCreatedAt(), savedTest.getCreatedAt());
        assertEquals(testToSave.getUpdatedAt(), savedTest.getUpdatedAt());
    }

    @org.junit.jupiter.api.Test
    void shouldThrowExceptionWhenSavingTestWithoutTitle() {
        // given
        TestType testType = new TestType(1L, TestTypeName.FUNCTIONAL);
        Test testToSave = new Test(null, 1L, 1L, 1L, null, "Test description",
                "Test instructions", "https://github.com/user/repo", testType, Instant.now(), Instant.now());

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> testRepository.save(testToSave));
    }

    @org.junit.jupiter.api.Test
    void shouldReturnTestWhenFindById() {
        // when
        Optional<Test> optionalTest = testRepository.findById(1L);

        // then
        assertTrue(optionalTest.isPresent());
        Test test = optionalTest.get();

        assertEquals(1L, test.getId());
        assertEquals(1L, test.getProjectId());
        assertEquals(1L, test.getTestCaseId());
        assertEquals(1L, test.getAuthorId());
        assertEquals("Test title", test.getTitle());
        assertEquals("Test description", test.getDescription());
        assertEquals("Test instructions", test.getInstructions());
        assertEquals("https://github.com/user/repo", test.getGithubUrl());
        assertEquals(TestTypeName.UNIT, test.getType().getName());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), test.getCreatedAt());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), test.getUpdatedAt());
    }

    @org.junit.jupiter.api.Test
    void shouldUpdateTestWhenChangingTitle() {
        // given
        Test testToUpdate = testRepository.findById(1L).orElseThrow();
        testToUpdate.setTitle("Updated Test Title");

        // when
        Test updatedTest = testRepository.save(testToUpdate);

        // then
        assertEquals(testToUpdate.getId(), updatedTest.getId());
        assertEquals("Updated Test Title", updatedTest.getTitle());
    }

    @org.junit.jupiter.api.Test
    void shouldDeleteTestWhenDeletingByExistingTest() {
        // given
        Test testToDelete = testRepository.findById(1L).orElseThrow();

        // when
        testRepository.delete(testToDelete);

        // then
        assertTrue(testRepository.findById(1L).isEmpty());
    }

    @org.junit.jupiter.api.Test
    void shouldDeleteTestByIdWhenDeletingByExistingId() {
        // when
        testRepository.deleteById(1L);

        // then
        assertTrue(testRepository.findById(1L).isEmpty());
    }

    @org.junit.jupiter.api.Test
    void shouldReturnTrueWhenTestExists() {
        // when
        boolean exists = testRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @org.junit.jupiter.api.Test
    void shouldReturnFalseWhenTestDoesNotExist() {
        // when
        boolean exists = testRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @org.junit.jupiter.api.Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<Test> tests = testRepository.findAll();

        // then
        assertNotNull(tests);
        assertFalse(tests.isEmpty());
    }
}
