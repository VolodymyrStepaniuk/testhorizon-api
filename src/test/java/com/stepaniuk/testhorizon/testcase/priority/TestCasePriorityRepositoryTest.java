package com.stepaniuk.testhorizon.testcase.priority;

import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import com.stepaniuk.testhorizon.types.testcase.TestCasePriorityName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/testcase/test_case_priorities.sql"})
class TestCasePriorityRepositoryTest {

    @Autowired
    private TestCasePriorityRepository testCasePriorityRepository;

    @Test
    void shouldSaveTestCasePriority() {
        // given
        TestCasePriority testCasePriority = new TestCasePriority(null, TestCasePriorityName.HIGH);

        // when
        TestCasePriority savedTestCasePriority = testCasePriorityRepository.save(testCasePriority);

        // then
        assertNotNull(savedTestCasePriority);
        assertNotNull(savedTestCasePriority.getId());
        assertEquals(testCasePriority.getName(), savedTestCasePriority.getName());
    }

    @Test
    void shouldThrowExceptionWhenSavingTestCasePriorityWithNullName() {
        // given
        TestCasePriority testCasePriority = new TestCasePriority(null, null);

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> testCasePriorityRepository.save(testCasePriority));
    }

    @Test
    void shouldReturnTestCasePriorityWhenFindById() {
        // when
        TestCasePriority testCasePriority = testCasePriorityRepository.findById(1L).orElseThrow();

        // then
        assertNotNull(testCasePriority);
        assertEquals(1L, testCasePriority.getId());
        assertEquals(TestCasePriorityName.LOW, testCasePriority.getName());
    }

    @Test
    void shouldReturnTestCasePriorityWhenFindByName() {
        // when
        TestCasePriority testCasePriority = testCasePriorityRepository.findByName(TestCasePriorityName.LOW).orElseThrow();

        // then
        assertNotNull(testCasePriority);
        assertEquals(1L, testCasePriority.getId());
        assertEquals(TestCasePriorityName.LOW, testCasePriority.getName());
    }

    @Test
    void shouldUpdateTestCasePriorityWhenChangingName() {
        // given
        TestCasePriority testCasePriority = testCasePriorityRepository.findById(1L).orElseThrow();
        testCasePriority.setName(TestCasePriorityName.HIGH);

        // when
        TestCasePriority updatedTestCasePriority = testCasePriorityRepository.save(testCasePriority);

        // then
        assertNotNull(updatedTestCasePriority);
        assertEquals(testCasePriority.getId(), updatedTestCasePriority.getId());
        assertEquals(TestCasePriorityName.HIGH, updatedTestCasePriority.getName());
    }

    @Test
    void shouldDeleteTestCasePriorityWhenDeletingByExistingTestCasePriority() {
        // given
        TestCasePriority testCasePriority = testCasePriorityRepository.findById(1L).orElseThrow();

        // when
        testCasePriorityRepository.delete(testCasePriority);

        // then
        assertFalse(testCasePriorityRepository.findById(1L).isPresent());
    }

    @Test
    void shouldDeleteTestCasePriorityWhenDeletingByExistingTestCasePriorityId() {
        // when
        testCasePriorityRepository.deleteById(1L);

        // then
        assertFalse(testCasePriorityRepository.findById(1L).isPresent());
    }

    @Test
    void shouldReturnTrueWhenExistsByExistingTestCasePriorityId() {
        // when
        boolean exists = testCasePriorityRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenExistsByNonExistingTestCasePriorityId() {
        // when
        boolean exists = testCasePriorityRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNotEmptyListWhenFindAll() {
        // when
        List<TestCasePriority> testCasePriorities = testCasePriorityRepository.findAll();

        // then
        assertNotNull(testCasePriorities);
        assertFalse(testCasePriorities.isEmpty());
    }
}
