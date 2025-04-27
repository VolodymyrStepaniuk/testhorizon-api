package com.stepaniuk.testhorizon.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.payload.user.UserUpdateRequest;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.testspecific.ControllerLevelUnitTest;
import com.stepaniuk.testhorizon.testspecific.jwt.WithJwtToken;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByEmailException;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchAuthorityException;
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
import java.util.Set;

import static com.stepaniuk.testhorizon.testspecific.hamcrest.TemporalStringMatchers.instantComparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerLevelUnitTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private PageMapper pageMapper;

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnUserResponseWhenGetUserById() throws Exception {
        // given
        var userId = 1L;
        var response = createUserResponse();

        when(userService.getUserById(eq(userId), any())).thenReturn(response);

        // when && then
        // @formatter:off
        mockMvc.perform(get("/users/" + userId)
                        .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(response.getEmail())))
                .andExpect(jsonPath("$.firstName", is(response.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(response.getLastName())))
                .andExpect(jsonPath("$.totalRating", is(response.getTotalRating())))
                .andExpect(jsonPath("$.authorities[0]", is("ADMIN")))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/users/1")));
        // @formatter:on
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenGetUserById() throws Exception {
        // given
        Long userId = 1L;
        when(userService.getUserById(eq(userId), any())).thenThrow(new NoSuchUserByIdException(1L));

        // when && then
        // @formatter:off
        mockMvc.perform(get("/users/" + userId)
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with id " + userId)))
                .andExpect(jsonPath("$.instance", is("/users")));
        // @formatter:on
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnUserResponseWhenGetUserByEmail() throws Exception {
        // given
        var email = "email@mail.com";
        var response = createUserResponse();

        // when
        when(userService.getUserByEmail(eq(email), any())).thenReturn(response);

        // then
        mockMvc.perform(get("/users/email/" + email)
                        .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(response.getEmail())))
                .andExpect(jsonPath("$.firstName", is(response.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(response.getLastName())))
                .andExpect(jsonPath("$.totalRating", is(response.getTotalRating())))
                .andExpect(jsonPath("$.authorities[0]", is("ADMIN")))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenGetUserByEmail() throws Exception {
        // given
        var email = "email@mail.com";

        // when
        when(userService.getUserByEmail(eq(email), any())).thenThrow(new NoSuchUserByEmailException(email));

        // then
        mockMvc.perform(get("/users/email/" + email)
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with email " + email)))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnUserResponseWhenUpdatingUser() throws Exception {
        // given
        var userId = 1L;
        var userRequest = new UserUpdateRequest("newFirstName", null, null);
        var response = createUserResponse();

        // when
        when(userService.updateUser(eq(userId), eq(userRequest), any(), any())).thenReturn(response);

        // then
        mockMvc.perform(patch("/users/" + userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRequest))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(response.getEmail())))
                .andExpect(jsonPath("$.firstName", is(response.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(response.getLastName())))
                .andExpect(jsonPath("$.totalRating", is(response.getTotalRating())))
                .andExpect(jsonPath("$.authorities[0]", is("ADMIN")))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenUpdatingUser() throws Exception {
        // given
        var userId = 1L;
        var userRequest = new UserUpdateRequest("newFirstName", null, null);

        // when
        when(userService.updateUser(eq(userId), eq(userRequest), any(), any())).thenThrow(new NoSuchUserByIdException(userId));

        // then
        mockMvc.perform(patch("/users/" + userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with id " + userId)))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenUpdatingUser() throws Exception {
        // given
        var userId = 1L;
        var userRequest = new UserUpdateRequest("newFirstName", null, null);

        // when
        when(userService.updateUser(eq(userId), eq(userRequest), any(), any())).thenThrow(new AccessToManageEntityDeniedException("User", "/users"));

        // then
        mockMvc.perform(patch("/users/" + userId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRequest))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage User denied")))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoContentWhenDeletingUser() throws Exception {
        // given
        long userId = 1L;
        // when && then
        mockMvc.perform(delete("/users/" + userId)
                        .contentType("application/json")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenDeletingUser() throws Exception {
        // given
        var userId = 1L;

        // when
        doThrow(new NoSuchUserByIdException(userId))
                .when(userService)
                .deleteUserById(eq(userId), any(), any());

        // then
        mockMvc.perform(delete("/users/" + userId)
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with id " + userId)))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseAccessToManageEntityDeniedExceptionWhenDeletingUser() throws Exception {
        // given
        var userId = 1L;

        // when
        doThrow(new AccessToManageEntityDeniedException("User", "/users"))
                .when(userService)
                .deleteUserById(eq(userId), any(), any());

        // then
        mockMvc.perform(delete("/users/" + userId)
                        .contentType("application/json")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage User denied")))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfUserResponsesWhenGettingAllUsers() throws Exception {
        // given
        var response = createUserResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(userService.getAllUsers(eq(pageable), eq(null), eq(null), eq(null),  any())).thenReturn(pageMapper.toResponse(
                new PageImpl<>(List.of(response), pageable, 1),
                URI.create("/users"))
        );

        // then

        mockMvc.perform(get("/users")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.users[0].email", is(response.getEmail())))
                .andExpect(jsonPath("$._embedded.users[0].firstName", is(response.getFirstName())))
                .andExpect(jsonPath("$._embedded.users[0].lastName", is(response.getLastName())))
                .andExpect(jsonPath("$._embedded.users[0].totalRating", is(response.getTotalRating())))
                .andExpect(jsonPath("$._embedded.users[0].authorities[0]", is("ADMIN")))
                .andExpect(jsonPath("$._embedded.users[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.users[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.users[0]._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfUserResponsesWhenGettingAllUsersWhenUserIdsNotEmpty() throws Exception {
        // given
        var response = createUserResponse();
        var pageable = PageRequest.of(0, 2);
        var userIds = List.of(1L);

        // when
        when(userService.getAllUsers(eq(pageable), eq(userIds), eq(null), eq(null),  any())).thenReturn(pageMapper.toResponse(
                new PageImpl<>(List.of(response), pageable, 1),
                URI.create("/users"))
        );

        // then
        mockMvc.perform(get("/users")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("ids", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.users[0].email", is(response.getEmail())))
                .andExpect(jsonPath("$._embedded.users[0].firstName", is(response.getFirstName())))
                .andExpect(jsonPath("$._embedded.users[0].lastName", is(response.getLastName())))
                .andExpect(jsonPath("$._embedded.users[0].totalRating", is(response.getTotalRating())))
                .andExpect(jsonPath("$._embedded.users[0].authorities[0]", is("ADMIN")))
                .andExpect(jsonPath("$._embedded.users[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.users[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.users[0]._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfUserResponsesWhenGettingAllUsersWhenEmailNotNull() throws Exception {
        // given
        var response = createUserResponse();
        var pageable = PageRequest.of(0, 2);
        var email = "email@mail.com";

        // when
        when(userService.getAllUsers(eq(pageable), eq(null), eq(email), eq(null),  any())).thenReturn(pageMapper.toResponse(
                new PageImpl<>(List.of(response), pageable, 1),
                URI.create("/users"))
        );

        // then
        mockMvc.perform(get("/users")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("email", email)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.users[0].email", is(response.getEmail())))
                .andExpect(jsonPath("$._embedded.users[0].firstName", is(response.getFirstName())))
                .andExpect(jsonPath("$._embedded.users[0].lastName", is(response.getLastName())))
                .andExpect(jsonPath("$._embedded.users[0].totalRating", is(response.getTotalRating())))
                .andExpect(jsonPath("$._embedded.users[0].authorities[0]", is("ADMIN")))
                .andExpect(jsonPath("$._embedded.users[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.users[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.users[0]._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfUserResponsesWhenGettingAllUsersWhenFullNameNotNull() throws Exception {
        // given
        var response = createUserResponse();
        var pageable = PageRequest.of(0, 2);
        var fullName = "first";
        // when
        when(userService.getAllUsers(eq(pageable), eq(null), eq(null), eq(fullName),  any())).thenReturn(pageMapper.toResponse(
                new PageImpl<>(List.of(response), pageable, 1),
                URI.create("/users"))
        );

        // then
        mockMvc.perform(get("/users")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "2")
                        .param("fullName", fullName)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.users[0].email", is(response.getEmail())))
                .andExpect(jsonPath("$._embedded.users[0].firstName", is(response.getFirstName())))
                .andExpect(jsonPath("$._embedded.users[0].lastName", is(response.getLastName())))
                .andExpect(jsonPath("$._embedded.users[0].totalRating", is(response.getTotalRating())))
                .andExpect(jsonPath("$._embedded.users[0].authorities[0]", is("ADMIN")))
                .andExpect(jsonPath("$._embedded.users[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.users[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.users[0]._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnUserResponseWhenGettingMe() throws Exception {
        // given
        var userId = 1L;
        var response = createUserResponse();

        when(userService.getUserById(eq(userId), any())).thenReturn(response);

        // when && then
        // @formatter:off
        mockMvc.perform(get("/users/me")
                        .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(response.getEmail())))
                .andExpect(jsonPath("$.firstName", is(response.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(response.getLastName())))
                .andExpect(jsonPath("$.totalRating", is(response.getTotalRating())))
                .andExpect(jsonPath("$.authorities[0]", is("ADMIN")))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/users/1")));
        // @formatter:on
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenGettingMe() throws Exception {
        // given
        var userId = 1L;

        when(userService.getUserById(eq(userId), any())).thenThrow(new NoSuchUserByIdException(userId));

        // when && then
        // @formatter:off
        mockMvc.perform(get("/users/me")
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with id " + userId)))
                .andExpect(jsonPath("$.instance", is("/users")));
        // @formatter:on
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnUserResponseWhenUpdatingMe() throws Exception {
        // given
        var userId = 1L;
        var userRequest = new UserUpdateRequest(null, "newFirstName", null);
        var response = createUserResponse();

        // when
        when(userService.updateUser(eq(userId), eq(userRequest), any(), any())).thenReturn(response);

        // then
        mockMvc.perform(patch("/users/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRequest))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(response.getEmail())))
                .andExpect(jsonPath("$.firstName", is(response.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(response.getLastName())))
                .andExpect(jsonPath("$.totalRating", is(response.getTotalRating())))
                .andExpect(jsonPath("$.authorities[0]", is("ADMIN")))
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenUpdatingMe() throws Exception {
        // given
        var userId = 1L;
        var userRequest = new UserUpdateRequest("newFirstName", null, null);

        // when
        when(userService.updateUser(eq(userId), eq(userRequest), any(), any())).thenThrow(new NoSuchUserByIdException(userId));

        // then
        mockMvc.perform(patch("/users/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with id " + userId)))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoContentWhenDeletingMe() throws Exception {
        // when && then

        mockMvc.perform(delete("/users/me")
                        .contentType("application/json")
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnErrorResponseWhenDeletingMe() throws Exception {
        // given
        var userId = 1L;

        // when
        doThrow(new NoSuchUserByIdException(userId))
                .when(userService)
                .deleteUserById(eq(userId), any(), any());

        // then
        mockMvc.perform(delete("/users/me")
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with id " + userId)))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnPageOfUserResponsesWhenGettingTopUsersByRating() throws Exception {
        // given
        var response = createUserResponse();
        var pageable = PageRequest.of(0, 1);

        // when
        when(userService.getTopUsersByRating(eq(pageable), any())).thenReturn(pageMapper.toResponse(
                new PageImpl<>(List.of(response), pageable, 1),
                URI.create("/users"))
        );

        // then
        mockMvc.perform(get("/users/top")
                        .contentType("application/json")
                        .param("page", "0")
                        .param("size", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users[0].id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$._embedded.users[0].email", is(response.getEmail())))
                .andExpect(jsonPath("$._embedded.users[0].firstName", is(response.getFirstName())))
                .andExpect(jsonPath("$._embedded.users[0].lastName", is(response.getLastName())))
                .andExpect(jsonPath("$._embedded.users[0].totalRating", is(response.getTotalRating())))
                .andExpect(jsonPath("$._embedded.users[0].authorities[0]", is("ADMIN")))
                .andExpect(jsonPath("$._embedded.users[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.users[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.users[0]._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNoContentWhenChangingUserAuthority() throws Exception {
        // given
        long userId = 1L;
        var authority = AuthorityName.ADMIN;

        // when & then
        mockMvc.perform(patch("/users/change-authority/" + userId)
                        .param("authority", authority.name())
                        .contentType("application/json")
                )
                .andExpect(status().isNoContent());

        verify(userService).changeUserAuthority(eq(userId), eq(authority), any());
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnNotFoundWhenChangingAuthorityForNonExistentUser() throws Exception {
        // given
        var userId = 2L;
        var authority = AuthorityName.DEVELOPER;

        // when
        doThrow(new NoSuchUserByIdException(userId))
                .when(userService)
                .changeUserAuthority(eq(userId), eq(authority), any());

        // then
        mockMvc.perform(patch("/users/change-authority/" + userId)
                        .param("authority", authority.name())
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with id " + userId)))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnForbiddenWhenNonAdminChangesUserAuthority() throws Exception {
        // given
        var userId = 2L;
        var authority = AuthorityName.ADMIN;

        // when
        doThrow(new AccessToManageEntityDeniedException("User", "/users"))
                .when(userService)
                .changeUserAuthority(eq(userId), eq(authority), any());

        // then
        mockMvc.perform(patch("/users/change-authority/" + userId)
                        .param("authority", authority.name())
                        .contentType("application/json")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.title", is("Access denied")))
                .andExpect(jsonPath("$.detail", is("Access to manage User denied")))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    @WithJwtToken(userId = 1L)
    void shouldReturnBadRequestWhenAuthorityNotFound() throws Exception {
        // given
        var userId = 2L;
        var authority = AuthorityName.DEVELOPER;

        // when
        doThrow(new NoSuchAuthorityException(authority))
                .when(userService)
                .changeUserAuthority(eq(userId), eq(authority), any());

        // then
        mockMvc.perform(patch("/users/change-authority/" + userId)
                        .param("authority", authority.name())
                        .contentType("application/json")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such authority")))
                .andExpect(jsonPath("$.detail", is("No authority with name " + authority)))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    private UserResponse createUserResponse() {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var response = new UserResponse(1L, "email@gmail.com", "firstName",
                "lastName", 120, Set.of(AuthorityName.ADMIN),timeOfCreation, timeOfModification);

        response.add(Link.of("http://localhost/users/1", "self"));
        response.add(Link.of("http://localhost/users/1", "update"));
        response.add(Link.of("http://localhost/users/1", "delete"));

        return response;
    }
}
