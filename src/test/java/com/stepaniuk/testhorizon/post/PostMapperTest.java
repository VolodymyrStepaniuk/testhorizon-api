package com.stepaniuk.testhorizon.post;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.post.PostResponse;
import com.stepaniuk.testhorizon.post.category.PostCategory;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

@MapperLevelUnitTest
@ContextConfiguration(classes = {PostMapperImpl.class})
class PostMapperTest {

    @Autowired
    private PostMapper postMapper;

    @Test
    void shouldMapPostToPostResponse() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        PostCategory postCategory = new PostCategory(1L, PostCategoryName.AUTOMATION_QA);

        Post post = new Post(
                null, 1L, "Post title", "Post description", "Post content", postCategory, timeOfCreation, timeOfModification
        );
        UserInfo owner = new UserInfo(1L, "firstName", "lastName");

        // when
        PostResponse postResponse = postMapper.toResponse(post, owner);
        // then

        assertNotNull(postResponse);
        assertNull(postResponse.getId());
        assertNotNull(postResponse.getOwner());
        assertEquals(post.getOwnerId(), postResponse.getOwner().getId());
        assertEquals(owner.getFirstName(), postResponse.getOwner().getFirstName());
        assertEquals(owner.getLastName(), postResponse.getOwner().getLastName());
        assertEquals(post.getTitle(), postResponse.getTitle());
        assertEquals(post.getDescription(), postResponse.getDescription());
        assertEquals(post.getContent(), postResponse.getContent());
        assertEquals(post.getCategory().getName(), postResponse.getCategory());
        assertEquals(post.getCreatedAt(), postResponse.getCreatedAt());
        assertEquals(post.getUpdatedAt(), postResponse.getUpdatedAt());
        assertTrue(postResponse.hasLinks());
        assertTrue(postResponse.getLinks().hasLink("self"));
        assertTrue(postResponse.getLinks().hasLink("update"));
        assertTrue(postResponse.getLinks().hasLink("delete"));
    }
}
