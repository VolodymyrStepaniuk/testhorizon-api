package com.stepaniuk.testhorizon.test.type;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestTypeRepository extends JpaRepository<TestType, Long> {
    Optional<TestType> findByName(TestTypeName name);
}