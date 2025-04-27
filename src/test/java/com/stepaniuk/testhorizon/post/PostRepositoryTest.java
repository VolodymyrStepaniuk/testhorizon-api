package com.stepaniuk.testhorizon.post;

import com.stepaniuk.testhorizon.post.category.PostCategory;
import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/post/post_categories.sql", "classpath:sql/post/posts.sql"})
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    void shouldSavePost() {
        // given
        PostCategory category = new PostCategory(1L, PostCategoryName.QUALITY_ASSURANCE);
        Post postToSave = new Post(null, 1L, "New Post", "Post description",
                "Post content", category, Instant.now(), Instant.now());

        // when
        Post savedPost = postRepository.save(postToSave);

        // then
        assertNotNull(savedPost);
        assertNotNull(savedPost.getId());
        assertEquals(postToSave.getOwnerId(), savedPost.getOwnerId());
        assertEquals(postToSave.getTitle(), savedPost.getTitle());
        assertEquals(postToSave.getDescription(), savedPost.getDescription());
        assertEquals(postToSave.getContent(), savedPost.getContent());
        assertEquals(postToSave.getCategory(), savedPost.getCategory());
        assertEquals(postToSave.getCreatedAt(), savedPost.getCreatedAt());
        assertEquals(postToSave.getUpdatedAt(), savedPost.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingPostWithoutTitle() {
        // given
        PostCategory category = new PostCategory(1L, PostCategoryName.QUALITY_ASSURANCE);
        Post postToSave = new Post(null, 1L, null, "Post description",
                "Post content", category, Instant.now(), Instant.now());

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> postRepository.save(postToSave));
    }

    @Test
    void shouldThrowExceptionWhenSavingPostWithoutContent() {
        // given
        PostCategory category = new PostCategory(1L, PostCategoryName.QUALITY_ASSURANCE);
        Post postToSave = new Post(null, 1L, "New Post", "Post description",
                null, category, Instant.now(), Instant.now());

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> postRepository.save(postToSave));
    }

    @Test
    void shouldReturnPostWhenFindById() {
        // when
        Optional<Post> optionalPost = postRepository.findById(1L);

        // then
        assertTrue(optionalPost.isPresent());
        Post post = optionalPost.get();

        assertEquals(1L, post.getId());
        assertEquals(1L, post.getOwnerId());
        assertEquals("Post 1", post.getTitle());
        assertEquals("Description", post.getDescription());
        assertEquals("Content of post 1", post.getContent());
        assertNotNull(post.getCategory());
        assertEquals(1L, post.getCategory().getId());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), post.getCreatedAt());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), post.getUpdatedAt());
    }

    @Test
    void shouldReturnOwnerIdWhenFindById() {
        // when
        Optional<Post> optionalPost = postRepository.findById(1L);

        // then
        assertTrue(optionalPost.isPresent());
        Post post = optionalPost.get();

        assertEquals(1L, post.getOwnerId());
    }

    @Test
    void shouldUpdatePostWhenChangingTitle() {
        // given
        Post postToUpdate = postRepository.findById(1L).orElseThrow();
        postToUpdate.setTitle("Updated Post Title");

        // when
        Post updatedPost = postRepository.save(postToUpdate);

        // then
        assertEquals(postToUpdate.getId(), updatedPost.getId());
        assertEquals("Updated Post Title", updatedPost.getTitle());
    }

    @Test
    void shouldUpdatePostWhenChangingContent() {
        // given
        Post postToUpdate = postRepository.findById(1L).orElseThrow();
        postToUpdate.setContent("Updated post content");

        // when
        Post updatedPost = postRepository.save(postToUpdate);

        // then
        assertEquals(postToUpdate.getId(), updatedPost.getId());
        assertEquals("Updated post content", updatedPost.getContent());
    }

    @Test
    void shouldDeletePostWhenDeletingByExistingPost() {
        // given
        Post postToDelete = postRepository.findById(1L).orElseThrow();

        // when
        postRepository.delete(postToDelete);

        // then
        assertTrue(postRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeletePostByIdWhenDeletingByExistingId() {
        // when
        postRepository.deleteById(1L);

        // then
        assertTrue(postRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenPostExists() {
        // when
        boolean exists = postRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenPostDoesNotExist() {
        // when
        boolean exists = postRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<Post> posts = postRepository.findAll();

        // then
        assertNotNull(posts);
        assertFalse(posts.isEmpty());
    }

}
