package com.stepaniuk.testhorizon.rating;

import com.stepaniuk.testhorizon.payload.rating.RatingResponse;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@MapperLevelUnitTest
@ContextConfiguration(classes = {RatingMapperImpl.class})
class RatingMapperTest {

    @Autowired
    private RatingMapper ratingMapper;

    @Test
    void shouldMapRatingToRatingResponse() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Rating rating = new Rating(null, 1L, 1L, 5, "comment", timeOfCreation);

        // when
        RatingResponse ratingResponse = ratingMapper.toResponse(rating);

        // then
        assertNotNull(ratingResponse);
        assertNull(ratingResponse.getId());
        assertEquals(ratingResponse.getUserId(), rating.getUserId());
        assertEquals(ratingResponse.getRatedByUserId(), rating.getRatedByUserId());
        assertEquals(ratingResponse.getRatingPoints(), rating.getRatingPoints());
        assertEquals(ratingResponse.getComment(), rating.getComment());
        assertEquals(ratingResponse.getCreatedAt(), rating.getCreatedAt());
        assertTrue(ratingResponse.hasLinks());
        assertTrue(ratingResponse.getLinks().hasLink("self"));
    }
}
