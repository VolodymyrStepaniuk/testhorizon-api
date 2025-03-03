package com.stepaniuk.testhorizon.rating;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
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
        var userInfo = new UserInfo(1L, "firstName", "lastName");
        var ratedByUserInfo = new UserInfo(1L, "firstName1", "lastName1");
        RatingResponse ratingResponse = ratingMapper.toResponse(rating, userInfo, ratedByUserInfo);

        // then
        assertNotNull(ratingResponse);
        assertNull(ratingResponse.getId());
        assertNotNull(ratingResponse.getUser());
        assertEquals(userInfo.getFirstName(), ratingResponse.getUser().getFirstName());
        assertEquals(userInfo.getLastName(), ratingResponse.getUser().getLastName());
        assertNotNull(ratingResponse.getRatedByUser());
        assertEquals(ratedByUserInfo.getFirstName(), ratingResponse.getRatedByUser().getFirstName());
        assertEquals(ratedByUserInfo.getLastName(), ratingResponse.getRatedByUser().getLastName());
        assertEquals(rating.getRatingPoints(), ratingResponse.getRatingPoints());
        assertEquals(rating.getComment(), ratingResponse.getComment());
        assertEquals(timeOfCreation, ratingResponse.getCreatedAt());
        assertTrue(ratingResponse.hasLinks());
        assertTrue(ratingResponse.getLinks().hasLink("self"));
    }
}
