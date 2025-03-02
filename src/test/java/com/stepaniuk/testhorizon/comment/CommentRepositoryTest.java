package com.stepaniuk.testhorizon.comment;

import com.stepaniuk.testhorizon.types.entity.EntityType;
import com.stepaniuk.testhorizon.testspecific.JpaLevelTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JpaLevelTest
@Sql(scripts = {"classpath:sql/comment/comments.sql"})
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void shouldSaveComment() {
        // given
        Comment commentToSave = new Comment(null, 1L, EntityType.TEST, 1L, "Comment content", Instant.now(), Instant.now());

        // when
        Comment savedComment = commentRepository.save(commentToSave);

        // then
        assertNotNull(savedComment);
        assertNotNull(savedComment.getId());
        assertEquals(commentToSave.getEntityType(), savedComment.getEntityType());
        assertEquals(commentToSave.getEntityId(), savedComment.getEntityId());
        assertEquals(commentToSave.getContent(), savedComment.getContent());
        assertEquals(commentToSave.getCreatedAt(), savedComment.getCreatedAt());
        assertEquals(commentToSave.getUpdatedAt(), savedComment.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingCommentWithoutContent() {
        // given
        Comment commentToSave = new Comment(null, 1L, EntityType.TEST, 1L, null, Instant.now(), Instant.now());

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> commentRepository.save(commentToSave));
    }

    @Test
    void shouldReturnCommentWhenFindById() {
        // when
        Optional<Comment> optionalComment = commentRepository.findById(1L);

        // then
        assertTrue(optionalComment.isPresent());
        Comment comment = optionalComment.get();

        assertEquals(1L, comment.getId());
        assertEquals(EntityType.TEST, comment.getEntityType());
        assertEquals(1L, comment.getEntityId());
        assertEquals("Comment content", comment.getContent());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), comment.getCreatedAt());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), comment.getUpdatedAt());
    }

    @Test
    void shouldUpdateCommentWhenChangingContent() {
        // given
        Comment commentToUpdate = commentRepository.findById(1L).orElseThrow();
        commentToUpdate.setContent("Updated Comment Content");

        // when
        Comment updatedComment = commentRepository.save(commentToUpdate);

        // then
        assertEquals(commentToUpdate.getId(), updatedComment.getId());
        assertEquals("Updated Comment Content", updatedComment.getContent());
    }

    @Test
    void shouldDeleteCommentWhenDeletingByExistingComment() {
        // given
        Comment commentToDelete = commentRepository.findById(1L).orElseThrow();

        // when
        commentRepository.delete(commentToDelete);

        // then
        assertTrue(commentRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeleteCommentByIdWhenDeletingByExistingId() {
        // when
        commentRepository.deleteById(1L);

        // then
        assertTrue(commentRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenCommentExists() {
        // when
        boolean exists = commentRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenCommentDoesNotExist() {
        // when
        boolean exists = commentRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<Comment> comments = commentRepository.findAll();

        // then
        assertNotNull(comments);
        assertFalse(comments.isEmpty());
    }
}
