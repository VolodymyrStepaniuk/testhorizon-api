package com.stepaniuk.testhorizon.comment;


import com.stepaniuk.testhorizon.types.entity.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
    Page<Comment> findByEntityTypeAndEntityId(Pageable pageable, EntityType entityType, Long entityId);
}