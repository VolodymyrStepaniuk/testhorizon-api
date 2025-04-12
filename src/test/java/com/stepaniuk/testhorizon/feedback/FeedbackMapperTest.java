package com.stepaniuk.testhorizon.feedback;

import com.stepaniuk.testhorizon.payload.feedback.FeedbackResponse;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@MapperLevelUnitTest
@ContextConfiguration(classes = {FeedbackMapperImpl.class})
class FeedbackMapperTest {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Test
    void shouldMapFeedbackToFeedbackResponse() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        Feedback feedback = new Feedback(null, 1L, 5,"Feedback content", timeOfCreation, timeOfModification);
        var userInfo = new UserInfo(1L, "John", "Doe");
        // when
        FeedbackResponse feedbackResponse = feedbackMapper.toResponse(feedback,userInfo);

        // then
        assertNotNull(feedbackResponse);
        assertEquals(feedback.getId(), feedbackResponse.getId());
        assertEquals(feedback.getRating(), feedbackResponse.getRating());
        assertEquals(feedback.getComment(), feedbackResponse.getComment());
        assertEquals(feedback.getOwnerId(), feedbackResponse.getOwner().getId());
        assertEquals(feedback.getCreatedAt(), feedbackResponse.getCreatedAt());
        assertEquals(feedback.getUpdatedAt(), feedbackResponse.getUpdatedAt());
    }
}
