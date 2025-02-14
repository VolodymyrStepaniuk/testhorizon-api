package com.stepaniuk.testhorizon.comment;

import com.stepaniuk.testhorizon.types.comment.CommentEntityType;
import com.stepaniuk.testhorizon.payload.comment.CommentResponse;
import com.stepaniuk.testhorizon.payload.comment.user.UserInfo;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@MapperLevelUnitTest
@ContextConfiguration(classes = {CommentMapperImpl.class})
class CommentMapperTest {

    @Autowired
    private CommentMapper commentMapper;

    @Test
    void shouldMapCommentToCommentResponse() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        Comment comment = new Comment(null, 1L, CommentEntityType.TEST,
                1L, "Comment content", timeOfCreation, timeOfModification);
        UserInfo userInfo = new UserInfo("First Name", "Last Name");

        // when
        CommentResponse commentResponse = commentMapper.toResponse(comment, userInfo);

        // then
        assertNotNull(commentResponse);
        assertNull(commentResponse.getId());
        assertEquals(commentResponse.getEntityType(), comment.getEntityType());
        assertEquals(commentResponse.getEntityId(), comment.getEntityId());
        assertEquals(commentResponse.getContent(), comment.getContent());
        assertEquals(commentResponse.getAuthor().getFirstName(), userInfo.getFirstName());
        assertEquals(commentResponse.getAuthor().getLastName(), userInfo.getLastName());
        assertEquals(commentResponse.getCreatedAt(), comment.getCreatedAt());
        assertEquals(commentResponse.getUpdatedAt(), comment.getUpdatedAt());
        assertTrue(commentResponse.hasLinks());
        assertTrue(commentResponse.getLinks().hasLink("self"));
        assertTrue(commentResponse.getLinks().hasLink("update"));
        assertTrue(commentResponse.getLinks().hasLink("delete"));
    }
}
