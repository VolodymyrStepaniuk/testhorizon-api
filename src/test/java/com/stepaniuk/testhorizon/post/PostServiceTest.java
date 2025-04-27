package com.stepaniuk.testhorizon.post;

import com.stepaniuk.testhorizon.event.post.PostCreatedEvent;
import com.stepaniuk.testhorizon.event.post.PostDeletedEvent;
import com.stepaniuk.testhorizon.event.post.PostEvent;
import com.stepaniuk.testhorizon.event.post.PostUpdatedEvent;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.payload.post.PostCreateRequest;
import com.stepaniuk.testhorizon.payload.post.PostUpdateRequest;
import com.stepaniuk.testhorizon.post.category.PostCategory;
import com.stepaniuk.testhorizon.post.category.PostCategoryRepository;
import com.stepaniuk.testhorizon.post.exceptions.NoSuchPostByIdException;
import com.stepaniuk.testhorizon.post.exceptions.NoSuchPostCategoryByNameException;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.shared.UserInfoService;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {PostService.class, PostMapperImpl.class, PageMapperImpl.class})
class PostServiceTest {

    @Autowired
    private PostService postService;

    @MockitoBean
    private PostProducer postProducer;

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private PostCategoryRepository postCategoryRepository;

    @MockitoBean
    private UserInfoService userInfoService;

    @Test
    void shouldReturnPostResponseWhenCreatingPost() {
        // given
        PostCreateRequest postCreateRequest = new PostCreateRequest(
                "Заголовок публікації",
                "Контент публікації",
                "Опис публікації",
                PostCategoryName.QUALITY_ASSURANCE);

        var userInfo = new UserInfo(1L, "Ім'я", "Прізвище");
        var category = new PostCategory(1L, PostCategoryName.QUALITY_ASSURANCE);

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(postRepository.save(any())).thenAnswer(answer(getFakeSave(1L)));
        when(postCategoryRepository.findByName(PostCategoryName.QUALITY_ASSURANCE)).thenReturn(Optional.of(category));

        final var receivedEventWrapper = new PostCreatedEvent[1];
        when(postProducer.send(assertArg(event -> receivedEventWrapper[0] = (PostCreatedEvent) event)))
                .thenAnswer(answer(getFakeSendResult()));

        // when
        var postResponse = postService.createPost(postCreateRequest, 1L, UUID.randomUUID().toString());

        // then
        assertNotNull(postResponse);
        assertEquals(1L, postResponse.getOwner().getId());
        assertNotNull(postResponse.getOwner());
        assertEquals(userInfo.getFirstName(), postResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), postResponse.getOwner().getLastName());
        assertEquals(postCreateRequest.getTitle(), postResponse.getTitle());
        assertEquals(postCreateRequest.getDescription(), postResponse.getDescription());
        assertEquals(postCreateRequest.getContent(), postResponse.getContent());
        assertEquals(postCreateRequest.getCategory(), postResponse.getCategory());
        assertTrue(postResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(1L, receivedEvent.getPostId());
        assertEquals(postResponse.getOwner().getId(), receivedEvent.getOwnerId());

        verify(postRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchPostCategoryByNameExceptionWhenCreatingPost() {
        // given
        var correlationId = UUID.randomUUID().toString();
        PostCreateRequest postCreateRequest = new PostCreateRequest(
                "Title",
                "Content",
                "Description",
                PostCategoryName.QUALITY_ASSURANCE);

        when(postCategoryRepository.findByName(PostCategoryName.QUALITY_ASSURANCE)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchPostCategoryByNameException.class, () ->
                postService.createPost(postCreateRequest, 1L, correlationId));
    }

    @Test
    void shouldReturnPostResponseWhenGettingPostById() {
        // given
        Post post = getNewPostWithAllFields();
        var userInfo = new UserInfo(1L, "Name", "Surname");

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when
        var postResponse = postService.getPostById(1L);

        // then
        assertNotNull(postResponse);
        assertEquals(post.getId(), postResponse.getId());
        assertNotNull(postResponse.getOwner());
        assertEquals(post.getOwnerId(), postResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), postResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), postResponse.getOwner().getLastName());
        assertEquals(post.getTitle(), postResponse.getTitle());
        assertEquals(post.getDescription(), postResponse.getDescription());
        assertEquals(post.getContent(), postResponse.getContent());
        assertEquals(post.getCategory().getName(), postResponse.getCategory());
        assertEquals(post.getCreatedAt(), postResponse.getCreatedAt());
        assertEquals(post.getUpdatedAt(), postResponse.getUpdatedAt());
        assertTrue(postResponse.hasLinks());
    }

    @Test
    void shouldThrowNoSuchPostByIdExceptionWhenGettingPostById() {
        // given
        when(postRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchPostByIdException.class, () -> postService.getPostById(10L));
    }

    @Test
    void shouldUpdateAndReturnPostResponseWhenChangingPostTitle() {
        // given
        Post postToUpdate = getNewPostWithAllFields();
        var userInfo = new UserInfo(1L, "Name", "Surname");

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        var postUpdateRequest = new PostUpdateRequest("New title", null, null, null);
        var authInfo = new AuthInfo(1L, List.of());
        when(postRepository.findById(1L)).thenReturn(Optional.of(postToUpdate));
        when(postRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var receivedEventWrapper = new PostUpdatedEvent[1];
        when(postProducer.send(assertArg(event -> receivedEventWrapper[0] = (PostUpdatedEvent) event)))
                .thenAnswer(answer(getFakeSendResult()));

        // when
        var updatedPostResponse = postService.updatePost(1L, postUpdateRequest, UUID.randomUUID().toString(), authInfo);

        // then
        assertNotNull(updatedPostResponse);
        assertEquals(postToUpdate.getId(), updatedPostResponse.getId());
        assertNotNull(updatedPostResponse.getOwner());
        assertEquals(postToUpdate.getOwnerId(), updatedPostResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), updatedPostResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), updatedPostResponse.getOwner().getLastName());
        assertEquals(postUpdateRequest.getTitle(), updatedPostResponse.getTitle());
        assertEquals(postToUpdate.getDescription(), updatedPostResponse.getDescription());
        assertEquals(postToUpdate.getContent(), updatedPostResponse.getContent());
        assertEquals(postToUpdate.getCategory().getName(), updatedPostResponse.getCategory());
        assertEquals(postToUpdate.getCreatedAt(), updatedPostResponse.getCreatedAt());
        assertEquals(postToUpdate.getUpdatedAt(), updatedPostResponse.getUpdatedAt());
        assertTrue(updatedPostResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(updatedPostResponse.getId(), receivedEvent.getPostId());
        assertEquals(updatedPostResponse.getTitle(), receivedEvent.getData().getTitle());
        assertNull(receivedEvent.getData().getDescription());

        verify(postRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowNoSuchPostByIdExceptionWhenUpdatingPost() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        PostUpdateRequest postUpdateRequest = new PostUpdateRequest("New title", null, null, null);

        when(postRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchPostByIdException.class, () ->
                postService.updatePost(10L, postUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldThrowNoSuchPostCategoryByNameExceptionWhenUpdatingPost() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Post postToUpdate = getNewPostWithAllFields();
        var postUpdateRequest = new PostUpdateRequest(null, null, null, PostCategoryName.MANUAL_TESTING);

        when(postRepository.findById(1L)).thenReturn(Optional.of(postToUpdate));
        when(postCategoryRepository.findByName(PostCategoryName.MANUAL_TESTING)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchPostCategoryByNameException.class, () ->
                postService.updatePost(1L, postUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessDeniedExceptionWhenUpdatingPost() {
        // given
        var authInfo = new AuthInfo(2L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Post postToUpdate = getNewPostWithAllFields();
        var postUpdateRequest = new PostUpdateRequest("New title", null, null, null);

        when(postRepository.findById(1L)).thenReturn(Optional.of(postToUpdate));

        // when && then
        assertThrows(AccessToManageEntityDeniedException.class, () ->
                postService.updatePost(1L, postUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldDeleteAndReturnVoidWhenDeletingExistingPost() {
        // given
        Post postToDelete = getNewPostWithAllFields();
        var authInfo = new AuthInfo(1L, List.of());
        when(postRepository.findById(1L)).thenReturn(Optional.of(postToDelete));

        final var receivedEventWrapper = new PostDeletedEvent[1];
        when(postProducer.send(assertArg(event -> receivedEventWrapper[0] = (PostDeletedEvent) event)))
                .thenAnswer(answer(getFakeSendResult()));

        // when
        postService.deletePostById(1L, UUID.randomUUID().toString(), authInfo);

        // then
        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(postToDelete.getId(), receivedEvent.getPostId());

        verify(postRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowNoSuchPostByIdExceptionWhenDeletingPost() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        when(postRepository.findById(10L)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchPostByIdException.class, () ->
                postService.deletePostById(10L, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessToManageEntityDeniedExceptionWhenDeletingPost() {
        // given
        var authInfo = new AuthInfo(2L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Post postToDelete = getNewPostWithAllFields();
        when(postRepository.findById(1L)).thenReturn(Optional.of(postToDelete));

        // when && then
        assertThrows(AccessToManageEntityDeniedException.class, () ->
                postService.deletePostById(1L, correlationId, authInfo));
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllPosts() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        var userInfo = new UserInfo(1L, "Name", "Surname");

        var postToFind = new Post(1L, 1L, "Title", "Description", "Content",
                new PostCategory(1L, PostCategoryName.QUALITY_ASSURANCE),
                timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);
        Specification<Post> specification = Specification.where(null);

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(postRepository.findAll(specification, pageable))
                .thenReturn(new PageImpl<>(List.of(postToFind), pageable, 1));

        // when
        var postPageResponse = postService.getAllPosts(pageable, null, null, null);
        var postResponse = postPageResponse.getContent().iterator().next();

        // then
        assertNotNull(postPageResponse);
        assertNotNull(postPageResponse.getMetadata());
        assertEquals(1, postPageResponse.getMetadata().getTotalElements());
        assertEquals(1, postPageResponse.getContent().size());

        assertNotNull(postResponse);
        assertEquals(postToFind.getId(), postResponse.getId());
        assertNotNull(postResponse.getOwner());
        assertEquals(postToFind.getOwnerId(), postResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), postResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), postResponse.getOwner().getLastName());
        assertEquals(postToFind.getTitle(), postResponse.getTitle());
        assertEquals(postToFind.getDescription(), postResponse.getDescription());
        assertEquals(postToFind.getContent(), postResponse.getContent());
        assertEquals(postToFind.getCategory().getName(), postResponse.getCategory());
        assertEquals(postToFind.getCreatedAt(), postResponse.getCreatedAt());
        assertEquals(postToFind.getUpdatedAt(), postResponse.getUpdatedAt());
        assertTrue(postResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllPostsByOwnerId() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        Long ownerId = 1L;

        var userInfo = new UserInfo(1L, "Name", "Surname");
        var postToFind = new Post(1L, ownerId, "Title", "Description", "Content",
                new PostCategory(1L, PostCategoryName.QUALITY_ASSURANCE),
                timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(userInfoService.getUserInfo(ownerId)).thenReturn(userInfo);
        when(postRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(postToFind), pageable, 1));

        // when
        var postPageResponse = postService.getAllPosts(pageable, ownerId, null, null);
        var postResponse = postPageResponse.getContent().iterator().next();

        // then
        assertNotNull(postPageResponse);
        assertNotNull(postPageResponse.getMetadata());
        assertEquals(1, postPageResponse.getMetadata().getTotalElements());
        assertEquals(1, postPageResponse.getContent().size());

        assertNotNull(postResponse);
        assertEquals(postToFind.getId(), postResponse.getId());
        assertNotNull(postResponse.getOwner());
        assertEquals(ownerId, postResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), postResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), postResponse.getOwner().getLastName());
        assertEquals(postToFind.getTitle(), postResponse.getTitle());
        assertEquals(postToFind.getDescription(), postResponse.getDescription());
        assertEquals(postToFind.getContent(), postResponse.getContent());
        assertEquals(postToFind.getCategory().getName(), postResponse.getCategory());
        assertEquals(postToFind.getCreatedAt(), postResponse.getCreatedAt());
        assertEquals(postToFind.getUpdatedAt(), postResponse.getUpdatedAt());
        assertTrue(postResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllPostsByTitle() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        String title = "Title";
        var userInfo = new UserInfo(1L, "Name", "Surname");
        var postToFind = new Post(1L, 1L, title, "Description", "Content",
                new PostCategory(1L, PostCategoryName.QUALITY_ASSURANCE),
                timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(postRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(postToFind), pageable, 1));

        // when
        var postPageResponse = postService.getAllPosts(pageable, null, title, null);
        var postResponse = postPageResponse.getContent().iterator().next();

        // then
        assertNotNull(postPageResponse);
        assertNotNull(postPageResponse.getMetadata());
        assertEquals(1, postPageResponse.getMetadata().getTotalElements());
        assertEquals(1, postPageResponse.getContent().size());

        assertNotNull(postResponse);
        assertEquals(postToFind.getId(), postResponse.getId());
        assertNotNull(postResponse.getOwner());
        assertEquals(postToFind.getOwnerId(), postResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), postResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), postResponse.getOwner().getLastName());
        assertEquals(title, postResponse.getTitle());
        assertEquals(postToFind.getDescription(), postResponse.getDescription());
        assertEquals(postToFind.getContent(), postResponse.getContent());
        assertEquals(postToFind.getCategory().getName(), postResponse.getCategory());
        assertEquals(postToFind.getCreatedAt(), postResponse.getCreatedAt());
        assertEquals(postToFind.getUpdatedAt(), postResponse.getUpdatedAt());
        assertTrue(postResponse.hasLinks());
    }

    @Test
    void shouldReturnPagedModelWhenGettingAllPostsByCategory() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        PostCategoryName categoryName = PostCategoryName.QUALITY_ASSURANCE;

        var userInfo = new UserInfo(1L, "Name", "Surname");
        var postToFind = new Post(1L, 1L, "Title", "Description", "Content",
                new PostCategory(1L, categoryName),
                timeOfCreation, timeOfModification);

        var pageable = PageRequest.of(0, 2);

        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);
        when(postRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(postToFind), pageable, 1));
        when(postCategoryRepository.findByName(categoryName)).thenReturn(Optional.of(postToFind.getCategory()));

        // when
        var postPageResponse = postService.getAllPosts(pageable, null, null, categoryName);
        var postResponse = postPageResponse.getContent().iterator().next();

        // then
        assertNotNull(postPageResponse);
        assertNotNull(postPageResponse.getMetadata());
        assertEquals(1, postPageResponse.getMetadata().getTotalElements());
        assertEquals(1, postPageResponse.getContent().size());

        assertNotNull(postResponse);
        assertEquals(postToFind.getId(), postResponse.getId());
        assertNotNull(postResponse.getOwner());
        assertEquals(postToFind.getOwnerId(), postResponse.getOwner().getId());
        assertEquals(userInfo.getFirstName(), postResponse.getOwner().getFirstName());
        assertEquals(userInfo.getLastName(), postResponse.getOwner().getLastName());
        assertEquals(postToFind.getTitle(), postResponse.getTitle());
        assertEquals(postToFind.getDescription(), postResponse.getDescription());
        assertEquals(postToFind.getContent(), postResponse.getContent());
        assertEquals(categoryName, postResponse.getCategory());
        assertEquals(postToFind.getCreatedAt(), postResponse.getCreatedAt());
        assertEquals(postToFind.getUpdatedAt(), postResponse.getUpdatedAt());
        assertTrue(postResponse.hasLinks());
    }

    @Test
    void shouldThrowNoSuchPostCategoryByNameExceptionWhenGettingAllPostsByCategory() {
        // given
        PostCategoryName categoryName = PostCategoryName.QUALITY_ASSURANCE;
        Pageable pageable = PageRequest.of(0, 2);

        when(postCategoryRepository.findByName(categoryName)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchPostCategoryByNameException.class, () ->
                postService.getAllPosts(pageable, null, null, categoryName));
    }

    private Answer1<Post, Post> getFakeSave(long id) {
        return post -> {
            post.setId(id);
            return post;
        };
    }

    private Answer1<CompletableFuture<SendResult<String, PostEvent>>, PostEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("posts", event),
                        new RecordMetadata(new TopicPartition("posts", 0), 0L, 0, 0L, 0, 0)));
    }

    private Post getNewPostWithAllFields() {
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));
        PostCategory category = new PostCategory(1L, PostCategoryName.QUALITY_ASSURANCE);

        return new Post(1L, 1L, "Title", "Description", "Content",
                category, timeOfCreation, timeOfModification);
    }
}
