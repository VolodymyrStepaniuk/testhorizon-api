package com.stepaniuk.testhorizon.rating;

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
@Sql(scripts = {"classpath:sql/rating/rating.sql"})
class RatingRepositoryTest {

    @Autowired
    private RatingRepository ratingRepository;

    @Test
    void shouldSaveRating() {
        // given
        Instant createdAt = Instant.now();
        Rating ratingToSave = new Rating(null, 1L, 2L, 5, "Comment", createdAt);

        // when
        Rating savedRating = ratingRepository.save(ratingToSave);

        // then
        assertNotNull(savedRating);
        assertNotNull(savedRating.getId());
        assertEquals(ratingToSave.getUserId(), savedRating.getUserId());
        assertEquals(ratingToSave.getRatedByUserId(), savedRating.getRatedByUserId());
        assertEquals(ratingToSave.getRatingPoints(), savedRating.getRatingPoints());
        assertEquals(ratingToSave.getComment(), savedRating.getComment());
        assertEquals(ratingToSave.getCreatedAt(), savedRating.getCreatedAt());
    }

    @Test
    void shouldThrowExceptionWhenSavingRatingWithoutPoints() {
        // given
        Instant createdAt = Instant.now();
        Rating ratingToSave = new Rating(null, 1L, 2L, null, "Comment", createdAt);

        // when & then
        assertThrows(DataIntegrityViolationException.class, () -> ratingRepository.save(ratingToSave));
    }

    @Test
    void shouldReturnRatingWhenFindById() {
        // when
        Optional<Rating> optionalRating = ratingRepository.findById(1L);

        // then
        assertTrue(optionalRating.isPresent());
        Rating rating = optionalRating.get();

        assertEquals(1L, rating.getId());
        assertEquals(1L, rating.getUserId());
        assertEquals(2L, rating.getRatedByUserId());
        assertEquals(5, rating.getRatingPoints());
        assertEquals("Comment", rating.getComment());
        assertEquals(Instant.parse("2024-11-25T17:28:19.266615Z"), rating.getCreatedAt());
    }

    @Test
    void shouldUpdateRatingWhenChangingComment() {
        // given
        String comment = "New comment";
        Rating ratingToUpdate = ratingRepository.findById(1L).orElseThrow();
        ratingToUpdate.setComment(comment);

        // when
        Rating updatedRating = ratingRepository.save(ratingToUpdate);

        // then
        assertEquals(ratingToUpdate.getId(), updatedRating.getId());
        assertEquals(comment, updatedRating.getComment());
    }

    @Test
    void shouldDeleteRatingWhenDeletingByExistingRating() {
        // given
        Rating ratingToDelete = ratingRepository.findById(1L).orElseThrow();

        // when
        ratingRepository.delete(ratingToDelete);

        // then
        assertTrue(ratingRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldDeleteRatingByIdWhenDeletingByExistingId() {
        // when
        ratingRepository.deleteById(1L);

        // then
        assertTrue(ratingRepository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnTrueWhenRatingExists() {
        // when
        boolean exists = ratingRepository.existsById(1L);

        // then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenRatingDoesNotExist() {
        // when
        boolean exists = ratingRepository.existsById(100L);

        // then
        assertFalse(exists);
    }

    @Test
    void shouldReturnNonEmptyListWhenFindAll() {
        // when
        List<Rating> ratings = ratingRepository.findAll();

        // then
        assertNotNull(ratings);
        assertFalse(ratings.isEmpty());
    }
}
