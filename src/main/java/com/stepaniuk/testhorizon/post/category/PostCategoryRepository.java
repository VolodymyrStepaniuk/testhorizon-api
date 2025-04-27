package com.stepaniuk.testhorizon.post.category;

import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory, Long>, JpaSpecificationExecutor<PostCategory> {
    Optional<PostCategory> findByName(PostCategoryName name);
}