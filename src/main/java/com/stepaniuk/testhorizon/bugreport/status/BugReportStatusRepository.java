package com.stepaniuk.testhorizon.bugreport.status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BugReportStatusRepository extends JpaRepository<BugReportStatus, Long> {
    Optional<BugReportStatus> findByName(BugReportStatusName name);
}