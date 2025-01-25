package com.stepaniuk.testhorizon.rating;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.payload.rating.RatingResponse;
import com.stepaniuk.testhorizon.payload.rating.RatingUpdateRequest;
import com.stepaniuk.testhorizon.rating.exceptions.UserCannotChangeOwnRatingException;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.testspecific.ControllerLevelUnitTest;
import com.stepaniuk.testhorizon.user.User;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static com.stepaniuk.testhorizon.testspecific.hamcrest.TemporalStringMatchers.instantComparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerLevelUnitTest(controllers = RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
class RatingControllerTest {

    @MockitoBean
    private RatingService ratingService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;
    @Autowired
    private PageMapper pageMapper;

    @Test
    void shouldReturnRatingResponseWhenCreatingRating() throws Exception {
        // given
        Long userId = 1L;

        mockSecurityContext(userId);

        RatingUpdateRequest ratingUpdateRequest = new RatingUpdateRequest(1L, 5, "comment");

        var ratingResponse = createRatingResponse();

        // when
        when(ratingService.changeRating(eq(ratingUpdateRequest), eq(userId), any())).thenReturn(ratingResponse);

        // then
        mockMvc.perform(post("/ratings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(ratingUpdateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(ratingResponse.getId()), Long.class))
                .andExpect(jsonPath("$.userId", is(ratingResponse.getUserId()), Long.class))
                .andExpect(jsonPath("$.ratedByUserId", is(ratingResponse.getRatedByUserId()), Long.class))
                .andExpect(jsonPath("$.ratingPoints", is(ratingResponse.getRatingPoints())))
                .andExpect(jsonPath("$.comment", is(ratingResponse.getComment())))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(ratingResponse.getCreatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/ratings/1")));
    }

    @Test
    void shouldReturnErrorResponseWhenCreatingRating() throws Exception {
        // given
        Long userId = 1L;

        mockSecurityContext(userId);

        RatingUpdateRequest ratingUpdateRequest = new RatingUpdateRequest(1L, 5, "comment");

        // when
        when(ratingService.changeRating(eq(ratingUpdateRequest), eq(userId), any())).thenThrow(new NoSuchUserByIdException(1L));

        // then
        mockMvc.perform(post("/ratings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(ratingUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with id " + userId)))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    void shouldReturnErrorResponseWhenCreatingRatingForOwnUser() throws Exception {
        // given
        Long authUserId = 1L;
        Long userId = 1L;

        mockSecurityContext(authUserId);

        RatingUpdateRequest ratingUpdateRequest = new RatingUpdateRequest(userId, 5, "comment");

        // when
        when(ratingService.changeRating(eq(ratingUpdateRequest), eq(userId), any())).thenThrow(new UserCannotChangeOwnRatingException(userId));

        // then
        mockMvc.perform(post("/ratings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(ratingUpdateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("User cannot change own rating")))
                .andExpect(jsonPath("$.detail", is("User cannot change own rating: " + userId)))
                .andExpect(jsonPath("$.instance", is("/ratings")));
    }

    @Test
    void shouldReturnPageOfRatingResponsesWhenGettingAllRatings() throws Exception {
        // given
        var response = createRatingResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(ratingService.getRatings(pageable, null, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/ratings"))
                );

        // then

        mockMvc.perform(get("/ratings")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ratings[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.ratings[0].userId", is(response.getUserId()), Long.class))
                .andExpect(jsonPath("$._embedded.ratings[0].ratedByUserId", is(response.getRatedByUserId()), Long.class))
                .andExpect(jsonPath("$._embedded.ratings[0].ratingPoints", is(response.getRatingPoints())))
                .andExpect(jsonPath("$._embedded.ratings[0].comment", is(response.getComment())))
                .andExpect(jsonPath("$._embedded.ratings[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.ratings[0]._links.self.href", is("http://localhost/ratings/1")));
    }

    @Test
    void shouldReturnPageOfRatingResponsesWhenGettingRatingByUserId() throws Exception{
        // given
        Long userId = 1L;
        var response = createRatingResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(ratingService.getRatings(pageable, userId, null))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/ratings"))
                );

        // then

        mockMvc.perform(get("/ratings")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("userId", userId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ratings[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.ratings[0].userId", is(response.getUserId()), Long.class))
                .andExpect(jsonPath("$._embedded.ratings[0].ratedByUserId", is(response.getRatedByUserId()), Long.class))
                .andExpect(jsonPath("$._embedded.ratings[0].ratingPoints", is(response.getRatingPoints())))
                .andExpect(jsonPath("$._embedded.ratings[0].comment", is(response.getComment())))
                .andExpect(jsonPath("$._embedded.ratings[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.ratings[0]._links.self.href", is("http://localhost/ratings/1")));
    }

    @Test
    void shouldReturnPageOfRatingResponsesWhenGettingRatingByRatedByUserId() throws Exception{
        // given
        Long ratedByUserId = 1L;
        var response = createRatingResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(ratingService.getRatings(pageable, null, ratedByUserId))
                .thenReturn(
                        pageMapper.toResponse(new PageImpl<>(List.of(response), pageable, 1),
                                URI.create("/ratings"))
                );

        // then

        mockMvc.perform(get("/ratings")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("ratedByUserId", ratedByUserId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ratings[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.ratings[0].userId", is(response.getUserId()), Long.class))
                .andExpect(jsonPath("$._embedded.ratings[0].ratedByUserId", is(response.getRatedByUserId()), Long.class))
                .andExpect(jsonPath("$._embedded.ratings[0].ratingPoints", is(response.getRatingPoints())))
                .andExpect(jsonPath("$._embedded.ratings[0].comment", is(response.getComment())))
                .andExpect(jsonPath("$._embedded.ratings[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.ratings[0]._links.self.href", is("http://localhost/ratings/1")));
    }

    private void mockSecurityContext(Long userId) {
        User mockUser = new User(
                userId,
                "firstName",
                "lastName",
                "email",
                0,
                "password",
                true,
                true,
                true,
                true,
                Set.of(),
                Instant.now(),
                Instant.now()
        );

        // Мокання SecurityContext для передачі користувача
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(mockUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private RatingResponse createRatingResponse(){
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));

        var ratingResponse = new RatingResponse(1L, 1L, 1L,5, "comment", timeOfCreation);

        ratingResponse.add(Link.of("http://localhost/ratings/1", "self"));

        return ratingResponse;
    }
}
