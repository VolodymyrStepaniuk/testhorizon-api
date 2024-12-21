package com.stepaniuk.testhorizon.bugreport.severity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BugReportSeverityRepository extends JpaRepository<BugReportSeverity, Long> {
    Optional<BugReportSeverity> findByName(BugReportSeverityName name);
}