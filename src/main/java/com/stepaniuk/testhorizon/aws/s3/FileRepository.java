package com.stepaniuk.testhorizon.aws.s3;

import com.stepaniuk.testhorizon.types.files.FileEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File, UUID>, JpaSpecificationExecutor<File> {

    boolean existsByOriginalNameAndEntityTypeAndEntityId(String originalName, FileEntityType entityType, Long entityId);
}