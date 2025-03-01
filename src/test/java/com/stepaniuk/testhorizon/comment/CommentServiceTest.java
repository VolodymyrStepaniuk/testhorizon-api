package com.stepaniuk.testhorizon.comment;

import com.stepaniuk.testhorizon.comment.exceptions.CommentAuthorMismatchException;
import com.stepaniuk.testhorizon.comment.exceptions.NoSuchCommentByIdException;
import com.stepaniuk.testhorizon.event.comment.CommentCreatedEvent;
import com.stepaniuk.testhorizon.event.comment.CommentDeletedEvent;
import com.stepaniuk.testhorizon.event.comment.CommentEvent;
import com.stepaniuk.testhorizon.event.comment.CommentUpdatedEvent;
import com.stepaniuk.testhorizon.payload.comment.CommentCreateRequest;
import com.stepaniuk.testhorizon.payload.comment.CommentResponse;
import com.stepaniuk.testhorizon.payload.comment.CommentUpdateRequest;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.shared.UserInfoService;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import com.stepaniuk.testhorizon.types.comment.CommentEntityType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.stubbing.Answer1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {CommentService.class, CommentMapperImpl.class, PageMapperImpl.class})
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @MockitoBean
    private CommentProducer commentProducer;

    @MockitoBean
    private CommentRepository commentRepository;

    @MockitoBean
    private UserInfoService userInfoService;

    @Test
    void shouldReturnCommentResponseWhenCreatingComment() {
        // given
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(CommentEntityType.TEST, 1L, "Comment content");
        Long authorId = 1L;
        UserInfo userInfo = new UserInfo(1L, "First Name", "Last Name");

        when(userInfoService.getUserInfo(authorId)).thenReturn(userInfo);
        when(commentRepository.save(any())).thenAnswer(answer(getFakeSave(1L)));
        final var receivedEventWrapper = new CommentCreatedEvent[1];
        when(
                commentProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (CommentCreatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        CommentResponse commentResponse = commentService.createComment(commentCreateRequest, authorId, UUID.randomUUID().toString());

        // then
        assertNotNull(commentResponse);
        assertEquals(commentCreateRequest.getEntityType(), commentResponse.getEntityType());
        assertEquals(commentCreateRequest.getEntityId(), commentResponse.getEntityId());
        assertEquals(commentCreateRequest.getContent(), commentResponse.getContent());
        assertNotNull(commentResponse.getAuthor());
        assertEquals(authorId, commentResponse.getAuthor().getId());
        assertEquals(userInfo.getFirstName(), commentResponse.getAuthor().getFirstName());
        assertEquals(userInfo.getLastName(), commentResponse.getAuthor().getLastName());
        assertTrue(commentResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(commentResponse.getId(), receivedEvent.getCommentId());
        assertEquals(commentResponse.getEntityType(), receivedEvent.getEntityType());
        assertEquals(commentResponse.getEntityId(), receivedEvent.getEntityId());

        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void shouldReturnCommentResponseWhenUpdatingComment() {
        // given
        Long commentId = 1L;
        Long userId = 1L;
        var authInfo = new AuthInfo(1L, List.of());
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("Updated comment content");
        Comment comment = getNewCommentWithAllFields(commentId);
        UserInfo userInfo = new UserInfo(1L, "First Name", "Last Name");

        when(userInfoService.getUserInfo(userId)).thenReturn(userInfo);
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        final var receivedEventWrapper = new CommentUpdatedEvent[1];
        when(
                commentProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (CommentUpdatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        CommentResponse commentResponse = commentService.updateComment(commentId, userId, commentUpdateRequest, UUID.randomUUID().toString(), authInfo);

        // then
        assertNotNull(commentResponse);
        assertEquals(commentId, commentResponse.getId());
        assertEquals(commentUpdateRequest.getContent(), commentResponse.getContent());
        assertEquals(comment.getEntityType(), commentResponse.getEntityType());
        assertEquals(comment.getEntityId(), commentResponse.getEntityId());
        assertNotNull(commentResponse.getAuthor());
        assertEquals(userId, commentResponse.getAuthor().getId());
        assertEquals(userInfo.getFirstName(), commentResponse.getAuthor().getFirstName());
        assertEquals(userInfo.getLastName(), commentResponse.getAuthor().getLastName());
        assertEquals(comment.getCreatedAt(), commentResponse.getCreatedAt());
        assertEquals(comment.getUpdatedAt(), commentResponse.getUpdatedAt());
        assertTrue(commentResponse.hasLinks());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(commentResponse.getId(), receivedEvent.getCommentId());
        assertEquals(commentResponse.getContent(), receivedEvent.getContent());

        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    void shouldThrowNoSuchCommentByIdExceptionWhenUpdatingNonExistingComment() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Long commentId = 1L;
        Long userId = 1L;
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("Updated comment content");

        // when
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.empty());

        // then
        assertThrows(NoSuchCommentByIdException.class, () -> commentService.updateComment(commentId, userId, commentUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldThrowCommentAuthorMismatchExceptionWhenUpdatingCommentWithDifferentAuthor() {
        // given
        var authInfo = new AuthInfo(1L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Long commentId = 1L;
        Long userId = 2L;
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("Updated comment content");
        Comment comment = getNewCommentWithAllFields(commentId);

        // when
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));

        // then
        assertThrows(CommentAuthorMismatchException.class, () -> commentService.updateComment(commentId, userId, commentUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessToManageEntityDeniedExceptionWhenUpdatingCommentWithoutAccess() {
        // given
        var authInfo = new AuthInfo(2L, List.of());
        var correlationId = UUID.randomUUID().toString();
        Long commentId = 1L;
        Long userId = 1L;
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("Updated comment content");
        Comment comment = getNewCommentWithAllFields(commentId);

        // when
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));

        // then
        assertThrows(AccessToManageEntityDeniedException.class, () -> commentService.updateComment(commentId, userId, commentUpdateRequest, correlationId, authInfo));
    }

    @Test
    void shouldReturnVoidWhenDeletingComment() {
        // given
        Long commentId = 1L;
        Comment comment = getNewCommentWithAllFields(commentId);
        var authInfo = new AuthInfo(1L, List.of());

        // when
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));
        final var receivedEventWrapper = new CommentDeletedEvent[1];
        when(
                commentProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (CommentDeletedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        commentService.deleteCommentById(commentId, UUID.randomUUID().toString(), authInfo);

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(comment.getId(), receivedEvent.getCommentId());

        // then
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void shouldThrowNoSuchCommentByIdExceptionWhenDeletingNonExistingComment() {
        // given
        var correlationId = UUID.randomUUID().toString();
        Long commentId = 1L;
        var authInfo = new AuthInfo(1L, List.of());

        // when
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.empty());

        // then
        assertThrows(NoSuchCommentByIdException.class, () -> commentService.deleteCommentById(commentId, correlationId, authInfo));
    }

    @Test
    void shouldThrowAccessToManageEntityDeniedExceptionWhenDeletingCommentWithoutAccess() {
        // given
        var correlationId = UUID.randomUUID().toString();
        Long commentId = 1L;
        Comment comment = getNewCommentWithAllFields(commentId);
        var authInfo = new AuthInfo(2L, List.of());

        // when
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));

        // then
        assertThrows(AccessToManageEntityDeniedException.class, () -> commentService.deleteCommentById(commentId, correlationId, authInfo));
    }

    @Test
    void shouldReturnPagedModelOfCommentResponseWhenGettingAllComments() {
        // given
        var commentToFind = getNewCommentWithAllFields(1L);
        var userInfo = new UserInfo(1L, "First Name", "Last Name");

        var pageable = PageRequest.of(0, 2);

        when(commentRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(commentToFind), pageable, 1));
        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);

        // when
        var comments = commentService.getAllComments(pageable, null);
        var comment = comments.getContent().iterator().next();

        // then
        assertNotNull(comments);
        assertNotNull(comments.getMetadata());
        assertEquals(1, comments.getMetadata().getTotalElements());
        assertEquals(1, comments.getContent().size());

        assertNotNull(comment);
        assertEquals(commentToFind.getId(), comment.getId());
        assertEquals(commentToFind.getEntityType(), comment.getEntityType());
        assertEquals(commentToFind.getEntityId(), comment.getEntityId());
        assertEquals(commentToFind.getContent(), comment.getContent());
        assertNotNull(comment.getAuthor());
        assertEquals(userInfo.getId(), comment.getAuthor().getId());
        assertEquals(userInfo.getFirstName(), comment.getAuthor().getFirstName());
        assertEquals(userInfo.getLastName(), comment.getAuthor().getLastName());
        assertEquals(commentToFind.getCreatedAt(), comment.getCreatedAt());
        assertEquals(commentToFind.getUpdatedAt(), comment.getUpdatedAt());
        assertTrue(comment.hasLinks());
    }

    @Test
    void shouldReturnPagedModelOfCommentResponseWhenGettingAllCommentsByAuthorId() {
        // given
        var commentToFind = getNewCommentWithAllFields(1L);
        var userInfo = new UserInfo(1L, "First Name", "Last Name");
        Long authorId = 1L;

        var pageable = PageRequest.of(0, 2);

        when(commentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(commentToFind), pageable, 1));
        when(userInfoService.getUserInfo(authorId)).thenReturn(userInfo);

        // when
        var comments = commentService.getAllComments(pageable, authorId);
        var comment = comments.getContent().iterator().next();

        // then
        assertNotNull(comments);
        assertNotNull(comments.getMetadata());
        assertEquals(1, comments.getMetadata().getTotalElements());
        assertEquals(1, comments.getContent().size());

        assertNotNull(comment);
        assertEquals(commentToFind.getId(), comment.getId());
        assertEquals(commentToFind.getEntityType(), comment.getEntityType());
        assertEquals(commentToFind.getEntityId(), comment.getEntityId());
        assertEquals(commentToFind.getContent(), comment.getContent());
        assertNotNull(comment.getAuthor());
        assertEquals(userInfo.getId(), comment.getAuthor().getId());
        assertEquals(userInfo.getFirstName(), comment.getAuthor().getFirstName());
        assertEquals(userInfo.getLastName(), comment.getAuthor().getLastName());
        assertEquals(commentToFind.getCreatedAt(), comment.getCreatedAt());
        assertEquals(commentToFind.getUpdatedAt(), comment.getUpdatedAt());
        assertTrue(comment.hasLinks());
    }

    @Test
    void shouldReturnPagedModelOfCommentResponseWhenGettingCommentsByEntity() {
        // given
        var commentToFind = getNewCommentWithAllFields(1L);
        var userInfo = new UserInfo(1L, "First Name", "Last Name");
        Long entityId = 1L;
        CommentEntityType entityType = CommentEntityType.TEST;

        var pageable = PageRequest.of(0, 2);

        when(commentRepository.findByEntityTypeAndEntityId(pageable, entityType, entityId)).thenReturn(new PageImpl<>(List.of(commentToFind), pageable, 1));
        when(userInfoService.getUserInfo(1L)).thenReturn(userInfo);

        // when
        var comments = commentService.getCommentsByEntity(pageable, entityId, entityType);
        var comment = comments.getContent().iterator().next();

        // then
        assertNotNull(comments);
        assertNotNull(comments.getMetadata());
        assertEquals(1, comments.getMetadata().getTotalElements());
        assertEquals(1, comments.getContent().size());

        assertNotNull(comment);
        assertEquals(commentToFind.getId(), comment.getId());
        assertEquals(entityType, comment.getEntityType());
        assertEquals(entityId, comment.getEntityId());
        assertEquals(commentToFind.getContent(), comment.getContent());
        assertNotNull(comment.getAuthor());
        assertEquals(userInfo.getId(), comment.getAuthor().getId());
        assertEquals(userInfo.getFirstName(), comment.getAuthor().getFirstName());
        assertEquals(userInfo.getLastName(), comment.getAuthor().getLastName());
        assertEquals(commentToFind.getCreatedAt(), comment.getCreatedAt());
        assertEquals(commentToFind.getUpdatedAt(), comment.getUpdatedAt());
        assertTrue(comment.hasLinks());
    }

    @Test
    void shouldReturnEmptyPagedModelOfCommentResponseWhenGettingCommentsByEntity() {
        // given
        Long entityId = 1L;
        CommentEntityType entityType = CommentEntityType.TEST;

        var pageable = PageRequest.of(0, 2);

        when(commentRepository.findByEntityTypeAndEntityId(pageable, entityType, entityId)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        var comments = commentService.getCommentsByEntity(pageable, entityId, entityType);

        // then
        assertNotNull(comments);
        assertNotNull(comments.getMetadata());
        assertEquals(0, comments.getMetadata().getTotalElements());
        assertEquals(0, comments.getContent().size());
    }

    private Answer1<Comment, Comment> getFakeSave(long id) {
        return comment -> {
            comment.setId(id);
            return comment;
        };
    }

    private Answer1<CompletableFuture<SendResult<String, CommentEvent>>, CommentEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("comments", event),
                        new RecordMetadata(new TopicPartition("comments", 0), 0L, 0, 0L, 0, 0)));
    }

    private Comment getNewCommentWithAllFields(Long id) {
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        return new Comment(id, 1L, CommentEntityType.TEST, 1L, "Comment content", timeOfCreation, timeOfModification);
    }
}
