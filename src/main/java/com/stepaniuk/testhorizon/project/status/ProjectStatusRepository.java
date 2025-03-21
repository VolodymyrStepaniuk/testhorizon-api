package com.stepaniuk.testhorizon.project.status;

import com.stepaniuk.testhorizon.types.project.ProjectStatusName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, Long> {
    Optional<ProjectStatus> findByName(ProjectStatusName name);
}