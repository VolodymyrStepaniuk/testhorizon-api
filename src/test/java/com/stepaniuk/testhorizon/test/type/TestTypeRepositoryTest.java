package com.stepaniuk.testhorizon.test.type;

import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/test/test_types.sql"})
class TestTypeRepositoryTest {

    @Autowired
    private TestTypeRepository testTypeRepository;

    @Test
    void shouldSaveTestType() {
        // given
        TestType testType = new TestType(null, TestTypeName.UNIT);

        // when
        TestType savedTestType = testTypeRepository.save(testType);

        // then
        assertNotNull(savedTestType);
        assertNotNull(savedTestType.getId());
        assertEquals(testType.getName(), savedTestType.getName());
    }

    @Test
    void shouldThrowExceptionWhenSavingTestTypeWithNullName() {
        // given
        TestType testType = new TestType(null, null);

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> testTypeRepository.save(testType));
    }

    @Test
    void shouldReturnTestTypeWhenFindById() {
        // when
        TestType testType = testTypeRepository.findById(1L).orElseThrow();

        // then
        assertNotNull(testType);
        assertEquals(1L, testType.getId());
        assertEquals(TestTypeName.UNIT, testType.getName());
    }

    @Test
    void shouldReturnTestTypeWhenFindByName() {
        // when
        TestType testType = testTypeRepository.findByName(TestTypeName.UNIT).orElseThrow();

        // then
        assertNotNull(testType);
        assertEquals(1L, testType.getId());
        assertEquals(TestTypeName.UNIT, testType.getName());
    }

    @Test
    void shouldUpdateTestTypeWhenChangingName() {
        // given
        TestType testType = testTypeRepository.findById(1L).orElseThrow();
        testType.setName(TestTypeName.INTEGRATION);

        // when
        TestType updatedTestType = testTypeRepository.save(testType);

        // then
        assertNotNull(updatedTestType);
        assertEquals(testType.getId(), updatedTestType.getId());
        assertEquals(TestTypeName.INTEGRATION, updatedTestType.getName());
    }

    @Test
    void shouldDeleteTestTypeWhenDeletingByExistingTestType() {
        // given
        TestType testType = testTypeRepository.findById(1L).orElseThrow();

        // when
        testTypeRepository.delete(testType);

        // then
        assertFalse(testTypeRepository.findById(1L).isPresent());
    }

    @Test
    void shouldDeleteTestTypeWhenDeletingByExistingTestTypeId() {
        // when
        testTypeRepository.deleteById(1L);

        // then
        assertFalse(testTypeRepository.findById(1L).isPresent());
    }

    @Test
    void shouldReturnTrueWhenExistsByExistingTestTypeId() {
        // when
        boolean exists = testTypeRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenExistsByNonExistingTestTypeId() {
        // when
        boolean exists = testTypeRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNotEmptyListWhenFindAll() {
        // when
        List<TestType> testTypes = testTypeRepository.findAll();

        // then
        assertNotNull(testTypes);
        assertFalse(testTypes.isEmpty());
    }
}
