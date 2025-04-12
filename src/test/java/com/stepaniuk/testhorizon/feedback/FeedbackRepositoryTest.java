package com.stepaniuk.testhorizon.feedback;

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
@Sql(scripts = {"classpath:sql/feedback/feedbacks.sql"})
class FeedbackRepositoryTest {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Test
    void shouldSaveFeedback() {
        // given
        Feedback feedbackToSave = new Feedback(null, 1L, 5, "Feedback Title", Instant.now(), Instant.now());

        // when
        Feedback savedFeedback = feedbackRepository.save(feedbackToSave);

        // then
        assertNotNull(savedFeedback);
        assertNotNull(savedFeedback.getId());
        assertEquals(feedbackToSave.getOwnerId(), savedFeedback.getOwnerId());
        assertEquals(feedbackToSave.getRating(), savedFeedback.getRating());
        assertEquals(feedbackToSave.getComment(), savedFeedback.getComment());
        assertEquals(feedbackToSave.getCreatedAt(), savedFeedback.getCreatedAt());
        assertEquals(feedbackToSave.getUpdatedAt(), savedFeedback.getUpdatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingFeedbackWithoutRating() {
        // given
        Feedback feedbackToSave = new Feedback(null, 1L, null, "Comment",
                Instant.now(), Instant.now());

        // when && then
        assertThrows(DataIntegrityViolationException.class, () -> feedbackRepository.save(feedbackToSave));
    }

    @Test
    void shouldReturnFeedbackWhenFindById() {
        // when
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(1L);

        // then
        assertTrue(optionalFeedback.isPresent());
        Feedback feedback = optionalFeedback.get();

        assertEquals(1L, feedback.getId());
        assertEquals(1L, feedback.getOwnerId());
        assertEquals(5, feedback.getRating());
        assertEquals("Great product!", feedback.getComment());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), feedback.getCreatedAt());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), feedback.getUpdatedAt());
    }

    @Test
    void shouldUpdateFeedbackWhenChangingComment() {
        // given
        Feedback feedbackToUpdate = feedbackRepository.findById(1L).orElseThrow();
        feedbackToUpdate.setComment("Updated Feedback Comment");

        // when
        Feedback updatedFeedback = feedbackRepository.save(feedbackToUpdate);

        // then
        assertEquals(feedbackToUpdate.getId(), updatedFeedback.getId());
        assertEquals(feedbackToUpdate.getComment(), updatedFeedback.getComment());
    }

    @Test
    void shouldDeleteFeedbackWhenDeletingByExistingFeedback() {
        // given
        Feedback feedbackToDelete = feedbackRepository.findById(1L).orElseThrow();

        // when
        feedbackRepository.delete(feedbackToDelete);

        // then
        assertTrue(feedbackRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeleteFeedbackByIdWhenDeletingByExistingId() {
        // when
        feedbackRepository.deleteById(1L);

        // then
        assertTrue(feedbackRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenFeedbackExists() {
        // when
        boolean exists = feedbackRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenFeedbackDoesNotExist() {
        // when
        boolean exists = feedbackRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<Feedback> feedbacks = feedbackRepository.findAll();

        // then
        assertNotNull(feedbacks);
        assertFalse(feedbacks.isEmpty());
    }
}
