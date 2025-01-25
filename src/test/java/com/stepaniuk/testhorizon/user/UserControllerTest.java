package com.stepaniuk.testhorizon.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.payload.user.UserUpdateRequest;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.testspecific.ControllerLevelUnitTest;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByEmailException;
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
    void shouldReturnUserResponseWhenGetUserById() throws Exception {
        // given
        var userId = 1L;
        var response = createUserResponse();

        when(userService.getUserById(userId)).thenReturn(response);

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
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/users/1")));
        // @formatter:on
    }

    @Test
    void shouldReturnErrorResponseWhenGetUserById() throws Exception {
        // given
        Long userId = 1L;
        when(userService.getUserById(userId)).thenThrow(new NoSuchUserByIdException(1L));

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
    void shouldReturnUserResponseWhenGetUserByEmail() throws Exception {
        // given
        var email = "email@mail.com";
        var response = createUserResponse();

        // when
        when(userService.getUserByEmail(email)).thenReturn(response);

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
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    void shouldReturnErrorResponseWhenGetUserByEmail() throws Exception {
        // given
        var email = "email@mail.com";

        // when
        when(userService.getUserByEmail(email)).thenThrow(new NoSuchUserByEmailException(email));

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
    void shouldReturnUserResponseWhenUpdatingUser() throws Exception {
        // given
        var userId = 1L;
        var userRequest = new UserUpdateRequest("newFirstName", null, null, null);
        var response = createUserResponse();

        // when
        when(userService.updateUser(eq(userId), eq(userRequest), any())).thenReturn(response);

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
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    void shouldReturnErrorResponseWhenUpdatingUser() throws Exception {
        // given
        var userId = 1L;
        var userRequest = new UserUpdateRequest("newFirstName", null, null, null);

        // when
        when(userService.updateUser(eq(userId), eq(userRequest), any())).thenThrow(new NoSuchUserByIdException(userId));

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
    void shouldReturnErrorResponseWhenDeletingUser() throws Exception {
        // given
        var userId = 1L;

        // when
        doThrow(new NoSuchUserByIdException(userId))
                .when(userService)
                .deleteUserById(eq(userId), any());

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
    void shouldReturnPageOfUserResponsesWhenGettingAllUsers() throws Exception {
        // given
        var response = createUserResponse();
        var pageable = PageRequest.of(0, 2);

        // when
        when(userService.getAllUsers(pageable, null)).thenReturn(pageMapper.toResponse(
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
                .andExpect(jsonPath("$._embedded.users[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.users[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.users[0]._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    void shouldReturnPageOfUserResponsesWhenGettingAllUsersWhenUserIdsNotEmpty() throws Exception {
        // given
        var response = createUserResponse();
        var pageable = PageRequest.of(0, 2);
        var userIds = List.of(1L);

        // when
        when(userService.getAllUsers(pageable, userIds)).thenReturn(pageMapper.toResponse(
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
                .andExpect(jsonPath("$._embedded.users[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.users[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.users[0]._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.delete.href", is("http://localhost/users/1")));
    }

    @Test
    void shouldReturnUserResponseWhenGettingMe() throws Exception {
        // given
        var userId = 1L;
        var response = createUserResponse();

        mockSecurityContext(userId);
        when(userService.getUserById(userId)).thenReturn(response);

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
                .andExpect(jsonPath("$.createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$.updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._links.delete.href", is("http://localhost/users/1")));
        // @formatter:on
    }

    @Test
    void shouldReturnErrorResponseWhenGettingMe() throws Exception {
        // given
        var userId = 1L;

        mockSecurityContext(userId);
        when(userService.getUserById(userId)).thenThrow(new NoSuchUserByIdException(userId));

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
    void shouldReturnPageOfUserResponsesWhenGettingTopUsersByRating() throws Exception {
        // given
        var response = createUserResponse();
        var pageable = PageRequest.of(0, 1);

        // when
        when(userService.getTopUsersByRating(pageable)).thenReturn(pageMapper.toResponse(
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
                .andExpect(jsonPath("$._embedded.users[0].createdAt", instantComparesEqualTo(response.getCreatedAt())))
                .andExpect(jsonPath("$._embedded.users[0].updatedAt", instantComparesEqualTo(response.getUpdatedAt())))
                .andExpect(jsonPath("$._embedded.users[0]._links.self.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.update.href", is("http://localhost/users/1")))
                .andExpect(jsonPath("$._embedded.users[0]._links.delete.href", is("http://localhost/users/1")));
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
                null,
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

    private UserResponse createUserResponse() {
        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var response = new UserResponse(1L, "email@mail.com", "firstName",
                "lastName", 120, timeOfCreation, timeOfModification);

        response.add(Link.of("http://localhost/users/1", "self"));
        response.add(Link.of("http://localhost/users/1", "update"));
        response.add(Link.of("http://localhost/users/1", "delete"));

        return response;
    }
}
