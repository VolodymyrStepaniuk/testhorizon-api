package com.stepaniuk.testhorizon.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import com.stepaniuk.testhorizon.payload.comment.CommentCreateRequest;
import com.stepaniuk.testhorizon.payload.comment.CommentResponse;
import com.stepaniuk.testhorizon.payload.comment.CommentUpdateRequest;
import com.stepaniuk.testhorizon.comment.exceptions.CommentAuthorMismatchException;
import com.stepaniuk.testhorizon.comment.exceptions.NoSuchCommentByIdException;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.testspecific.ControllerLevelUnitTest;
import com.stepaniuk.testhorizon.testspecific.jwt.WithJwtToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.stepaniuk.testhorizon.testspecific.hamcrest.TemporalStringMatchers.instantComparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerLevelUnitTest(controllers = CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private PageMapper pageMapper;

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnCommentResponseWhenCreatingComment() throws Exception {
        // given
        Long userId = 1L;

        CommentCreateRequest commentCreateRequest = new CommentCreateRequest(EntityType.TEST, 1L, "Comment content");

        CommentResponse commentResponse = createCommentResponse(1L);

        // when
        when(commentService.createComment(eq(commentCreateRequest), eq(userId), any())).thenReturn(commentResponse);

        // then
        mockMvc.perform(post("/comments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(commentResponse.getId()), Long.class))
                .andExpect(jsonPath("$.entityType", is(commentResponse.getEntityType().name())))
                .andExpect(jsonPath("$.entityId", is(commentResponse.getEntityId()), Long.class))
                .andExpect(jsonPath("$.content", is(commentResponse.getContent())))
                .andExpect(jsonPath("$.author.id", is(commentResponse.getAuthor().getId()), Long.class))
                .andExpect(jsonPath("$.author.firstName", is(commentResponse.getAuthor().getFirstName())))
                .andExpect(jsonPath("$.author.lastName", is(commentResponse.getAuthor().getLastName())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(commentResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(commentResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/comments/" + commentResponse.getId())))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/comments/" + commentResponse.getId())))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/comments/" + commentResponse.getId())));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnCommentResponseWhenUpdatingComment() throws Exception {
        // given
        Long userId = 1L;
        Long commentId = 1L;

        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("Updated comment content");

        CommentResponse commentResponse = createCommentResponse(commentId);

        // when
        when(commentService.updateComment(eq(commentId), eq(userId), eq(commentUpdateRequest), any(), any())).thenReturn(commentResponse);

        // then
        mockMvc.perform(patch("/comments/" + commentId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentResponse.getId()), Long.class))
                .andExpect(jsonPath("$.entityType", is(commentResponse.getEntityType().name())))
                .andExpect(jsonPath("$.entityId", is(commentResponse.getEntityId()), Long.class))
                .andExpect(jsonPath("$.content", is(commentResponse.getContent())))
                .andExpect(jsonPath("$.author.id", is(commentResponse.getAuthor().getId()), Long.class))
                .andExpect(jsonPath("$.author.firstName", is(commentResponse.getAuthor().getFirstName())))
                .andExpect(jsonPath("$.author.lastName", is(commentResponse.getAuthor().getLastName())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(commentResponse.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(commentResponse.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/comments/" + commentResponse.getId())))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/comments/" + commentResponse.getId())))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/comments/" + commentResponse.getId())));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseNoSuchCommentByIdExceptionWhenUpdatingComment() throws Exception {
        // given
        Long userId = 1L;
        Long commentId = 1L;

        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("Updated comment content");

        // when
        when(commentService.updateComment(eq(commentId), eq(userId), eq(commentUpdateRequest), any(), any())).thenThrow(new NoSuchCommentByIdException(commentId));

        // then
        mockMvc.perform(patch("/comments/" + commentId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentUpdateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such comment")))
                .andExpect(jsonPath("$.detail", is("No comment with id " + commentId)))
                .andExpect(jsonPath("$.instance", is("/comments")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseCommentAuthorMismatchExceptionWhenUpdatingComment() throws Exception {
        // given
        Long userId = 1L;
        Long commentId = 1L;

        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("Updated comment content");

        // when
        when(commentService.updateComment(eq(commentId), eq(userId), eq(commentUpdateRequest), any(), any())).thenThrow(new CommentAuthorMismatchException(commentId, userId));

        // then
        mockMvc.perform(patch("/comments/" + commentId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentUpdateRequest))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Comment author mismatch")))
                .andExpect(jsonPath("$.detail", is("Comment author mismatch: " + commentId)))
                .andExpect(jsonPath("$.instance", is("/comments")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenUpdatingComment() throws Exception {
        // given
        Long userId = 1L;
        Long commentId = 1L;

        CommentUpdateRequest commentUpdateRequest = new CommentUpdateRequest("Updated comment content");

        // when
        when(commentService.updateComment(eq(commentId), eq(userId), eq(commentUpdateRequest), any(), any())).
                thenThrow(new AccessToManageEntityDeniedException("Comment", "/comments"));

        // then
        mockMvc.perform(patch("/comments/" + commentId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentUpdateRequest))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage Comment denied")))
                .andExpect(jsonPath("$.instance", is("/comments")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoContentWhenDeletingComment() throws Exception {
        // given
        long commentId = 1L;

        // when
        mockMvc.perform(delete("/comments/" + commentId)
                        .contentType("application/json")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenDeletingComment() throws Exception {
        // given
        long commentId = 1L;

        doThrow(new NoSuchCommentByIdException(commentId))
                .when(commentService)
                .deleteCommentById(eq(commentId), any(), any());

        mockMvc.perform(delete("/comments/" + commentId)
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such comment")))
                .andExpect(jsonPath("$.detail", is("No comment with id " + commentId)))
                .andExpect(jsonPath("$.instance", is("/comments")));
    }

    @Test
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenDeletingComment() throws Exception {
        // given
        Long commentId = 1L;

        // when
        doThrow(new AccessToManageEntityDeniedException("Comment", "/comments"))
                .when(commentService)
                .deleteCommentById(eq(commentId), any(), any());

        // then
        mockMvc.perform(delete("/comments/" + commentId)
                        .contentType("application/json")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage Comment denied")))
                .andExpect(jsonPath("$.instance", is("/comments")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfCommentResponsesWhenGettingAllComments() throws Exception {
        // given
        Long commentId = 1L;
        var response = createCommentResponse(commentId);
        var pageable = PageRequest.of(0, 2);

        when(commentService.getAllComments(pageable, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/comments"))
                );

        // then
        mockMvc.perform(get("/comments/all")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.comments[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.comments[0].entityType", is(response.getEntityType().name())))
                .andExpect(jsonPath("$._embedded.comments[0].entityId", is(response.getEntityId()), Long.class))
                .andExpect(jsonPath("$._embedded.comments[0].content", is(response.getContent())))
                .andExpect(jsonPath("$._embedded.comments[0].author.id", is(response.getAuthor().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.comments[0].author.firstName", is(response.getAuthor().getFirstName())))
                .andExpect(jsonPath("$._embedded.comments[0].author.lastName", is(response.getAuthor().getLastName())))
                .andExpect(jsonPath("$._embedded.comments[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.comments[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.comments[0]._links.self.href", is("http://localhost/comments/" + response.getId())))
                .andExpect(jsonPath("$._embedded.comments[0]._links.update.href", is("http://localhost/comments/" + response.getId())))
                .andExpect(jsonPath("$._embedded.comments[0]._links.delete.href", is("http://localhost/comments/" + response.getId())));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfCommentResponsesWhenGettingAllCommentsWhenAuthorIdNotNull() throws Exception {
        // given
        Long commentId = 1L;
        Long authorId = 1L;
        var response = createCommentResponse(commentId);
        var pageable = PageRequest.of(0, 2);

        when(commentService.getAllComments(pageable, authorId))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/comments"))
                );

        // then
        mockMvc.perform(get("/comments/all")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("authorId", authorId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.comments[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.comments[0].entityType", is(response.getEntityType().name())))
                .andExpect(jsonPath("$._embedded.comments[0].entityId", is(response.getEntityId()), Long.class))
                .andExpect(jsonPath("$._embedded.comments[0].content", is(response.getContent())))
                .andExpect(jsonPath("$._embedded.comments[0].author.id", is(response.getAuthor().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.comments[0].author.firstName", is(response.getAuthor().getFirstName())))
                .andExpect(jsonPath("$._embedded.comments[0].author.lastName", is(response.getAuthor().getLastName())))
                .andExpect(jsonPath("$._embedded.comments[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.comments[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.comments[0]._links.self.href", is("http://localhost/comments/" + response.getId())))
                .andExpect(jsonPath("$._embedded.comments[0]._links.update.href", is("http://localhost/comments/" + response.getId())))
                .andExpect(jsonPath("$._embedded.comments[0]._links.delete.href", is("http://localhost/comments/" + response.getId())));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfCommentResponsesWhenGettingAllCommentsByEntity() throws Exception {
        // given
        Long commentId = 1L;
        Long entityId = 1L;
        EntityType entityType = EntityType.TEST;
        var response = createCommentResponse(commentId);
        var pageable = PageRequest.of(0, 2);

        when(commentService.getCommentsByEntity(pageable, entityId, entityType))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/comments"))
                );

        // then
        mockMvc.perform(get("/comments")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("entityId", entityId.toString())
                        .param("entityType", entityType.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.comments[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.comments[0].entityType", is(response.getEntityType().name())))
                .andExpect(jsonPath("$._embedded.comments[0].entityId", is(response.getEntityId()), Long.class))
                .andExpect(jsonPath("$._embedded.comments[0].content", is(response.getContent())))
                .andExpect(jsonPath("$._embedded.comments[0].author.id", is(response.getAuthor().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.comments[0].author.firstName", is(response.getAuthor().getFirstName())))
                .andExpect(jsonPath("$._embedded.comments[0].author.lastName", is(response.getAuthor().getLastName())))
                .andExpect(jsonPath("$._embedded.comments[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.comments[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.comments[0]._links.self.href", is("http://localhost/comments/" + response.getId())))
                .andExpect(jsonPath("$._embedded.comments[0]._links.update.href", is("http://localhost/comments/" + response.getId())))
                .andExpect(jsonPath("$._embedded.comments[0]._links.delete.href", is("http://localhost/comments/" + response.getId())));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnEmptyPageWhenGettingAllCommentsByEntity() throws Exception {
        // given
        Long entityId = 1L;
        EntityType entityType = EntityType.TEST;
        var pageable = PageRequest.of(0, 2);

        when(commentService.getCommentsByEntity(pageable, entityId, entityType))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(), pageable, 0),
                                URI.create("/comments"))
                );

        // then
        mockMvc.perform(get("/comments")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("entityId", entityId.toString())
                        .param("entityType", entityType.name())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(0)));
    }

    private CommentResponse createCommentResponse(Long id) {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var commentResponse = new CommentResponse(
                id,
                EntityType.TEST,
                1L,
                "Comment content",
                new UserInfo(1L,"firstName", "lastName"),
                timeOfCreation,
                timeOfModification
        );

        commentResponse.add(Link.of("http://localhost/comments/1", "self"));
        commentResponse.add(Link.of("http://localhost/comments/1", "update"));
        commentResponse.add(Link.of("http://localhost/comments/1", "delete"));

        return commentResponse;
    }
}
