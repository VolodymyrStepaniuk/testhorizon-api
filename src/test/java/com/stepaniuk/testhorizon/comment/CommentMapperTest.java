package com.stepaniuk.testhorizon.comment;

import com.stepaniuk.testhorizon.types.entity.EntityType;
import com.stepaniuk.testhorizon.payload.comment.CommentResponse;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
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

        Comment comment = new Comment(null, 1L, EntityType.TEST,
                1L, "Comment content", timeOfCreation, timeOfModification);
        UserInfo userInfo = new UserInfo(1L,"First Name", "Last Name");

        // when
        CommentResponse commentResponse = commentMapper.toResponse(comment, userInfo);

        // then
        assertNotNull(commentResponse);
        assertNull(commentResponse.getId());
        assertNotNull(commentResponse.getAuthor());
        assertEquals(comment.getAuthorId(), commentResponse.getAuthor().getId());
        assertEquals(userInfo.getFirstName(), commentResponse.getAuthor().getFirstName());
        assertEquals(userInfo.getLastName(), commentResponse.getAuthor().getLastName());
        assertEquals(comment.getEntityType(), commentResponse.getEntityType());
        assertEquals(comment.getEntityId(), commentResponse.getEntityId());
        assertEquals(comment.getContent(), commentResponse.getContent());
        assertEquals(comment.getCreatedAt(), commentResponse.getCreatedAt());
        assertEquals(comment.getUpdatedAt(), commentResponse.getUpdatedAt());
        assertTrue(commentResponse.hasLinks());
        assertTrue(commentResponse.getLinks().hasLink("self"));
        assertTrue(commentResponse.getLinks().hasLink("update"));
        assertTrue(commentResponse.getLinks().hasLink("delete"));
    }
}
