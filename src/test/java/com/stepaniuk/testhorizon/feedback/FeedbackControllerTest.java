package com.stepaniuk.testhorizon.feedback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.feedback.exceptions.NoSuchFeedbackFoundByIdException;
import com.stepaniuk.testhorizon.payload.feedback.FeedbackCreateRequest;
import com.stepaniuk.testhorizon.payload.feedback.FeedbackResponse;
import com.stepaniuk.testhorizon.payload.feedback.FeedbackUpdateRequest;
import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.testspecific.ControllerLevelUnitTest;
import com.stepaniuk.testhorizon.testspecific.jwt.WithJwtToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.stepaniuk.testhorizon.testspecific.hamcrest.TemporalStringMatchers.instantComparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerLevelUnitTest(controllers = FeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
class FeedbackControllerTest {

    @MockitoBean
    private FeedbackService feedbackService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;
    
    @Autowired
    private PageMapper pageMapper;

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnFeedbackResponseWhenCreatingFeedback() throws Exception {
        // given
        Long ownerId = 1L;

        FeedbackCreateRequest feedbackCreateRequest = new FeedbackCreateRequest(5, "Great service!");
        var response = getResponse();

        // when
        when(feedbackService.createFeedback(eq(feedbackCreateRequest), eq(ownerId), any())).thenReturn(response);

        // then
        mockMvc.perform(post("/feedbacks")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(feedbackCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.rating", is(response.getRating())))
                .andExpect(jsonPath("$.comment", is(response.getComment())))
                .andExpect(jsonPath("$.owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/feedbacks/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/feedbacks/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/feedbacks/1")));

        SecurityContextHolder.clearContext();
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnFeedbackResponseWhenGettingById() throws Exception {
        // given
        Long feedbackId = 1L;
        var response = getResponse();

        // when
        when(feedbackService.getFeedback(eq(feedbackId), any(AuthInfo.class))).thenReturn(response);

        // then
        mockMvc.perform(get("/feedbacks/" + feedbackId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.rating", is(response.getRating())))
                .andExpect(jsonPath("$.comment", is(response.getComment())))
                .andExpect(jsonPath("$.owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/feedbacks/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/feedbacks/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/feedbacks/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldThrowNoSuchFeedbackFoundByIdExceptionWhenGettingNonExistingFeedback() throws Exception {
        // given
        Long feedbackId = 1L;

        // when
        when(feedbackService.getFeedback(eq(feedbackId), any(AuthInfo.class)))
                .thenThrow(new NoSuchFeedbackFoundByIdException(feedbackId));

        // then
        mockMvc.perform(get("/feedbacks/" + feedbackId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such feedback")))
                .andExpect(jsonPath("$.detail", is("No feedback with id " + feedbackId)))
                .andExpect(jsonPath("$.instance", is("/feedbacks")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnFeedbackResponseWhenUpdatingFeedback() throws Exception {
        // given
        Long feedbackId = 1L;
        var feedbackUpdateRequest = new FeedbackUpdateRequest(4, "Updated comment");
        var response = getResponse();

        // when
        when(feedbackService.updateFeedback(eq(feedbackId), eq(feedbackUpdateRequest), any())).thenReturn(response);

        // then
        mockMvc.perform(patch("/feedbacks/" + feedbackId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(feedbackUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.rating", is(response.getRating())))
                .andExpect(jsonPath("$.comment", is(response.getComment())))
                .andExpect(jsonPath("$.owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$.owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$.owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/feedbacks/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/feedbacks/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/feedbacks/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenUpdatingNonExistingFeedback() throws Exception {
        // given
        Long feedbackId = 1L;
        var feedbackUpdateRequest = new FeedbackUpdateRequest(4, "Updated comment");

        // when
        when(feedbackService.updateFeedback(eq(feedbackId), eq(feedbackUpdateRequest), any()))
                .thenThrow(new NoSuchFeedbackFoundByIdException(feedbackId));

        // then
        mockMvc.perform(patch("/feedbacks/" + feedbackId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(feedbackUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such feedback")))
                .andExpect(jsonPath("$.detail", is("No feedback with id " + feedbackId)))
                .andExpect(jsonPath("$.instance", is("/feedbacks")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoContentWhenDeletingFeedback() throws Exception {
        // given
        long feedbackId = 1L;

        // when & then
        mockMvc.perform(delete("/feedbacks/" + feedbackId)
                        .contentType("application/json"))
                .andExpect(status().isNoContent());

        verify(feedbackService).deleteFeedback(eq(feedbackId), any());
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenDeletingNonExistingFeedback() throws Exception {
        // given
        Long feedbackId = 1L;

        // when
        doThrow(new NoSuchFeedbackFoundByIdException(feedbackId))
                .when(feedbackService)
                .deleteFeedback(eq(feedbackId), any());

        // then
        mockMvc.perform(delete("/feedbacks/" + feedbackId)
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such feedback")))
                .andExpect(jsonPath("$.detail", is("No feedback with id " + feedbackId)))
                .andExpect(jsonPath("$.instance", is("/feedbacks")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfFeedbackResponsesWhenGettingAllFeedbacks() throws Exception {
        // given
        var response = getResponse();
        var pageable = PageRequest.of(0, 10);

        // when
        when(feedbackService.getAllFeedbacks(eq(pageable), isNull(), isNull()))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/feedbacks"))
                );

        // then
        mockMvc.perform(get("/feedbacks")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.feedbacks[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.feedbacks[0].rating", is(response.getRating())))
                .andExpect(jsonPath("$._embedded.feedbacks[0].comment", is(response.getComment())))
                .andExpect(jsonPath("$._embedded.feedbacks[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.feedbacks[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.feedbacks[0].owner.lastName", is(response.getOwner().getLastName())))
                .andExpect(jsonPath("$._embedded.feedbacks[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.feedbacks[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.feedbacks[0]._links.self.href", is("http://localhost/feedbacks/1")))
                .andExpect(jsonPath("$._embedded.feedbacks[0]._links.update.href", is("http://localhost/feedbacks/1")))
                .andExpect(jsonPath("$._embedded.feedbacks[0]._links.delete.href", is("http://localhost/feedbacks/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfFeedbackResponsesWhenGettingAllFeedbacksWithOwnerIdFilter() throws Exception {
        // given
        var response = getResponse();
        var pageable = PageRequest.of(0, 10);
        Long ownerId = 1L;

        // when
        when(feedbackService.getAllFeedbacks(eq(pageable), eq(ownerId), isNull()))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/feedbacks"))
                );

        // then
        mockMvc.perform(get("/feedbacks")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "10")
                        .param("ownerId", ownerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.feedbacks[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.feedbacks[0].rating", is(response.getRating())))
                .andExpect(jsonPath("$._embedded.feedbacks[0].comment", is(response.getComment())))
                .andExpect(jsonPath("$._embedded.feedbacks[0].owner.id", is(response.getOwner().getId()), Long.class))
                .andExpect(jsonPath("$._embedded.feedbacks[0].owner.firstName", is(response.getOwner().getFirstName())))
                .andExpect(jsonPath("$._embedded.feedbacks[0].owner.lastName", is(response.getOwner().getLastName())));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfFeedbackResponsesWhenGettingAllFeedbacksWithFeedbackIdsFilter() throws Exception {
        // given
        var response = getResponse();
        var pageable = PageRequest.of(0, 10);
        List<Long> feedbackIds = List.of(1L);

        // when
        when(feedbackService.getAllFeedbacks(eq(pageable), isNull(), eq(feedbackIds)))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/feedbacks"))
                );

        // then
        mockMvc.perform(get("/feedbacks")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "10")
                        .param("feedbackIds", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.feedbacks[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.feedbacks[0].rating", is(response.getRating())))
                .andExpect(jsonPath("$._embedded.feedbacks[0].comment", is(response.getComment())))
                .andExpect(jsonPath("$._embedded.feedbacks[0].owner.id", is(response.getOwner().getId()), Long.class));
    }

    private FeedbackResponse getResponse() {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var response = new FeedbackResponse(
                1L,
                5,
                "Great service!",
                new UserInfo(1L, "firstName", "lastName"),
                timeOfCreation,
                timeOfModification
        );

        response.add(Link.of("http://localhost/feedbacks/1", "self"));
        response.add(Link.of("http://localhost/feedbacks/1", "update"));
        response.add(Link.of("http://localhost/feedbacks/1", "delete"));

        return response;
    }
}
