package com.stepaniuk.testhorizon.testcase.priority;

import com.stepaniuk.testhorizon.types.testcase.TestCasePriorityName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestCasePriorityRepository extends JpaRepository<TestCasePriority, Long> {
    Optional<TestCasePriority> findByName(TestCasePriorityName name);
}