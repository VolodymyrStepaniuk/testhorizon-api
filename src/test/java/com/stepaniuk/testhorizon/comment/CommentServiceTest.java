package com.stepaniuk.testhorizon.comment;

import com.stepaniuk.testhorizon.comment.type.CommentEntityType;
import com.stepaniuk.testhorizon.payload.comment.CommentCreateRequest;
import com.stepaniuk.testhorizon.payload.comment.CommentResponse;
import com.stepaniuk.testhorizon.payload.comment.CommentUpdateRequest;
import com.stepaniuk.testhorizon.payload.comment.exception.CommentAuthorMismatchException;
import com.stepaniuk.testhorizon.payload.comment.exception.NoSuchCommentByIdException;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.shared.PageMapperImpl;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import com.stepaniuk.testhorizon.user.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {CommentService.class, CommentMapperImpl.class, PageMapperImpl.class})
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @MockitoBean
    private CommentRepository commentRepository;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnCommentResponseWhenCreatingComment() {
        // given
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(CommentEntityType.TEST, 1L, "Comment content");
        Long authorId = 1L;
        UserResponse user = getNewUserResponseWithAllFields();

        when(userService.getUserById(authorId)).thenReturn(user);
        when(commentRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        // when
        CommentResponse commentResponse = commentService.createComment(commentCreateRequest, authorId);

        // then
        assertNotNull(commentResponse);
        assertEquals(commentResponse.getEntityType(), commentCreateRequest.getEntityType());
        assertEquals(commentResponse.getEntityId(), commentCreateRequest.getEntityId());
        assertEquals(commentResponse.getContent(), commentCreateRequest.getContent());
        assertNotNull(commentResponse.getAuthor());
        assertTrue(commentResponse.hasLinks());

        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void shouldReturnCommentResponseWhenUpdatingComment() {
        // given
        Long commentId = 1L;
        Long userId = 1L;
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("Updated comment content");
        Comment comment = getNewCommentWithAllFields(commentId);
        UserResponse user = getNewUserResponseWithAllFields();

        // when
        when(userService.getUserById(userId)).thenReturn(user);
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));
        when(commentRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        CommentResponse commentResponse = commentService.updateComment(commentId, userId, commentUpdateRequest);

        // then
        assertNotNull(commentResponse);
        assertEquals(commentId, commentResponse.getId());
        assertEquals(commentUpdateRequest.getContent(), commentResponse.getContent());
        assertEquals(comment.getEntityType(), commentResponse.getEntityType());
        assertEquals(comment.getEntityId(), commentResponse.getEntityId());
        assertEquals(user.getFirstName(), commentResponse.getAuthor().getFirstName());
        assertEquals(user.getLastName(), commentResponse.getAuthor().getLastName());
        assertEquals(comment.getCreatedAt(), commentResponse.getCreatedAt());
        assertEquals(comment.getUpdatedAt(), commentResponse.getUpdatedAt());
        assertTrue(commentResponse.hasLinks());

        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    void shouldThrowNoSuchCommentByIdExceptionWhenUpdatingNonExistingComment() {
        // given
        Long commentId = 1L;
        Long userId = 1L;
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("Updated comment content");

        // when
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.empty());

        // then
        assertThrows(NoSuchCommentByIdException.class, () -> commentService.updateComment(commentId, userId, commentUpdateRequest));
    }

    @Test
    void shouldThrowCommentAuthorMismatchExceptionWhenUpdatingCommentWithDifferentAuthor() {
        // given
        Long commentId = 1L;
        Long userId = 2L;
        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("Updated comment content");
        Comment comment = getNewCommentWithAllFields(commentId);

        // when
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));

        // then
        assertThrows(CommentAuthorMismatchException.class, () -> commentService.updateComment(commentId, userId, commentUpdateRequest));
    }

    @Test
    void shouldReturnVoidWhenDeletingComment() {
        // given
        Long commentId = 1L;
        Comment comment = getNewCommentWithAllFields(commentId);

        // when
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.of(comment));

        // when
        commentService.deleteCommentById(commentId);

        // then
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void shouldThrowNoSuchCommentByIdExceptionWhenDeletingNonExistingComment() {
        // given
        Long commentId = 1L;

        // when
        when(commentRepository.findById(commentId)).thenReturn(java.util.Optional.empty());

        // then
        assertThrows(NoSuchCommentByIdException.class, () -> commentService.deleteCommentById(commentId));
    }

    @Test
    void shouldReturnPagedModelOfCommentResponseWhenGettingAllComments() {
        // given
        var commentToFind = getNewCommentWithAllFields(1L);
        var user = getNewUserResponseWithAllFields();

        var pageable = PageRequest.of(0, 2);

        when(commentRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(commentToFind), pageable, 1));
        when(userService.getUserById(any())).thenReturn(user);

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
        assertEquals(commentToFind.getCreatedAt(), comment.getCreatedAt());
        assertEquals(commentToFind.getUpdatedAt(), comment.getUpdatedAt());
        assertTrue(comment.hasLinks());
    }

    @Test
    void shouldReturnPagedModelOfCommentResponseWhenGettingAllCommentsByAuthorId() {
        // given
        var commentToFind = getNewCommentWithAllFields(1L);
        var user = getNewUserResponseWithAllFields();
        Long authorId = 1L;

        var pageable = PageRequest.of(0, 2);

        when(commentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(commentToFind), pageable, 1));
        when(userService.getUserById(any())).thenReturn(user);

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
        assertEquals(user.getFirstName(), comment.getAuthor().getFirstName());
        assertEquals(user.getLastName(), comment.getAuthor().getLastName());
        assertEquals(commentToFind.getCreatedAt(), comment.getCreatedAt());
        assertEquals(commentToFind.getUpdatedAt(), comment.getUpdatedAt());
        assertTrue(comment.hasLinks());
    }

    @Test
    void shouldReturnPagedModelOfCommentResponseWhenGettingCommentsByEntity(){
        // given
        var commentToFind = getNewCommentWithAllFields(1L);
        var user = getNewUserResponseWithAllFields();
        Long entityId = 1L;
        CommentEntityType entityType = CommentEntityType.TEST;

        var pageable = PageRequest.of(0, 2);

        when(commentRepository.findByEntityTypeAndEntityId(pageable, entityType, entityId)).thenReturn(new PageImpl<>(List.of(commentToFind), pageable, 1));
        when(userService.getUserById(any())).thenReturn(user);

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
        assertEquals(user.getFirstName(), comment.getAuthor().getFirstName());
        assertEquals(user.getLastName(), comment.getAuthor().getLastName());
        assertEquals(commentToFind.getCreatedAt(), comment.getCreatedAt());
        assertEquals(commentToFind.getUpdatedAt(), comment.getUpdatedAt());
        assertTrue(comment.hasLinks());
    }

    @Test
    void shouldReturnEmptyPagedModelOfCommentResponseWhenGettingCommentsByEntity(){
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

    private Comment getNewCommentWithAllFields(Long id) {
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        return new Comment(id, 1L, CommentEntityType.TEST, 1L, "Comment content", timeOfCreation, timeOfModification);
    }

    private static UserResponse getNewUserResponseWithAllFields(){
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        return new UserResponse(1L, "email@gmail.com", "Doe", "Doe", 129, timeOfCreation, timeOfModification);
    }
}
