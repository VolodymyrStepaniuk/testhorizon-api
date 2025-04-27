package com.stepaniuk.testhorizon.post.category;

import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/post/post_categories.sql"})
class PostCategoryRepositoryTest {
    @Autowired
    private PostCategoryRepository postCategoryRepository;

    @Test
    void shouldSavePostCategory() {
        // given
        PostCategory postCategory = new PostCategory(null, PostCategoryName.QUALITY_ASSURANCE);

        // when
        PostCategory savedPostCategory = postCategoryRepository.save(postCategory);

        // then
        assertNotNull(savedPostCategory);
        assertNotNull(savedPostCategory.getId());
        assertEquals(postCategory.getName(), savedPostCategory.getName());
    }

    @Test
    void shouldThrowExceptionWhenSavingPostCategoryWithNullName() {
        // given
        PostCategory postCategory = new PostCategory(null, null);

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> postCategoryRepository.save(postCategory));
    }

    @Test
    void shouldReturnPostCategoryWhenFindById() {
        // when
        PostCategory postCategory = postCategoryRepository.findById(1L).orElseThrow();

        // then
        assertNotNull(postCategory);
        assertEquals(1L, postCategory.getId());
        assertEquals(PostCategoryName.QUALITY_ASSURANCE, postCategory.getName());
    }

    @Test
    void shouldReturnPostCategoryWhenFindByName() {
        // when
        PostCategory postCategory = postCategoryRepository.findByName(PostCategoryName.QUALITY_ASSURANCE).orElseThrow();

        // then
        assertNotNull(postCategory);
        assertEquals(1L, postCategory.getId());
        assertEquals(PostCategoryName.QUALITY_ASSURANCE, postCategory.getName());
    }

    @Test
    void shouldUpdatePostCategoryWhenChangingName() {
        // given
        PostCategory postCategory = postCategoryRepository.findById(1L).orElseThrow();
        postCategory.setName(PostCategoryName.MANUAL_TESTING);

        // when
        PostCategory updatedPostCategory = postCategoryRepository.save(postCategory);

        // then
        assertNotNull(updatedPostCategory);
        assertEquals(postCategory.getId(), updatedPostCategory.getId());
        assertEquals(PostCategoryName.MANUAL_TESTING, updatedPostCategory.getName());
    }

    @Test
    void shouldDeletePostCategoryWhenDeletingByExistingPostCategory() {
        // given
        PostCategory postCategory = postCategoryRepository.findById(1L).orElseThrow();

        // when
        postCategoryRepository.delete(postCategory);

        // then
        assertFalse(postCategoryRepository.findById(1L).isPresent());
    }

    @Test
    void shouldDeletePostCategoryWhenDeletingByExistingPostCategoryId() {
        // when
        postCategoryRepository.deleteById(1L);

        // then
        assertFalse(postCategoryRepository.findById(1L).isPresent());
    }

    @Test
    void shouldReturnTrueWhenExistsByExistingPostCategoryId() {
        // when
        boolean exists = postCategoryRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenExistsByNonExistingPostCategoryId() {
        // when
        boolean exists = postCategoryRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNotEmptyListWhenFindAll() {
        // when
        List<PostCategory> postCategories = postCategoryRepository.findAll();

        // then
        assertNotNull(postCategories);
        assertFalse(postCategories.isEmpty());
    }

}
