package com.stepaniuk.testhorizon.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stepaniuk.testhorizon.payload.auth.*;
import com.stepaniuk.testhorizon.payload.user.UserCreateRequest;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.security.auth.AuthenticationController;
import com.stepaniuk.testhorizon.security.auth.AuthenticationService;
import com.stepaniuk.testhorizon.security.auth.passwordreset.exception.NoSuchPasswordResetTokenException;
import com.stepaniuk.testhorizon.security.auth.passwordreset.exception.PasswordResetTokenExpiredException;
import com.stepaniuk.testhorizon.security.config.JwtAuthFilter;
import com.stepaniuk.testhorizon.security.exceptions.InvalidTokenException;
import com.stepaniuk.testhorizon.testspecific.ControllerLevelUnitTest;
import com.stepaniuk.testhorizon.user.authority.AuthorityName;
import com.stepaniuk.testhorizon.user.email.exceptions.InvalidVerificationCodeException;
import com.stepaniuk.testhorizon.user.email.exceptions.VerificationCodeExpiredException;
import com.stepaniuk.testhorizon.user.exceptions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.hateoas.Link;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static com.stepaniuk.testhorizon.testspecific.hamcrest.TemporalStringMatchers.instantComparesEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerLevelUnitTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @MockitoBean
    private AuthenticationService authenticationService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void shouldReturnUserResponseWhenRegister() throws Exception {
        // given
        var userCreateRequest = new UserCreateRequest("mail@gmail.com", "Qwerty@123", "John", "Doe", AuthorityName.TESTER);

        var timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        var timeOfModification = Instant.now().plus(Duration.ofHours(20));

        var response = new UserResponse(1L, userCreateRequest.getEmail(), userCreateRequest.getFirstName(), userCreateRequest.getLastName(),
                100, timeOfCreation, timeOfModification);

        response.add(Link.of("http://localhost/users/1", "self"));
        response.add(Link.of("http://localhost/users/1", "update"));
        response.add(Link.of("http://localhost/users/1", "delete"));

        when(authenticationService.register(eq(userCreateRequest), any())).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userCreateRequest))
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
    void shouldReturnErrorResponseWhenRegister() throws Exception {
        // given
        var userCreateRequest = new UserCreateRequest("mail@gmail.com", "Qwerty@123", "John", "Doe", AuthorityName.TESTER);

        when(authenticationService.register(eq(userCreateRequest), any())).thenThrow(new UserAlreadyExistsException(userCreateRequest.getEmail()));

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userCreateRequest))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.title", is("User already exists")))
                .andExpect(jsonPath("$.detail", is("User with email " + userCreateRequest.getEmail() + " already exists")))
                .andExpect(jsonPath("$.instance", is("/users")));

    }

    @Test
    void shouldReturnErrorResponseWithNoSuchAuthorityExceptionWhenRegister() throws Exception {
        // given
        var userCreateRequest = new UserCreateRequest("mail@gmail.com", "Qwerty@123", "John", "Doe", AuthorityName.TESTER);

        when(authenticationService.register(eq(userCreateRequest), any())).thenThrow(new NoSuchAuthorityException(userCreateRequest.getAuthorityName()));

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userCreateRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such authority")))
                .andExpect(jsonPath("$.detail", is("No authority with name " + userCreateRequest.getAuthorityName())))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    void shouldReturnAuthenticationResponseWhenLogin() throws Exception {
        // given
        var loginRequest = new LoginRequest("mail@gmail.com", "Qwerty@123");
        var authenticationResponse = AuthenticationResponse
                .builder()
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .build();

        when(authenticationService.authenticate(eq(loginRequest), any())).thenReturn(authenticationResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", is(authenticationResponse.getAccessToken())))
                .andExpect(jsonPath("$.refresh_token", is(authenticationResponse.getRefreshToken())));
    }

    @Test
    void shouldReturnErrorResponseWhenLogin() throws Exception {
        // given
        var loginRequest = new LoginRequest("mail@gmail.com", "Qwerty@123");

        when(authenticationService.authenticate(eq(loginRequest), any())).thenThrow(new NoSuchUserByEmailException(loginRequest.getEmail()));

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with email " + loginRequest.getEmail())))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    void shouldReturnErrorResponseThrowUserNotVerifiedExceptionWhenLogin() throws Exception {
        // given
        var loginRequest = new LoginRequest("mail@gmail.com", "Qwerty@123");

        when(authenticationService.authenticate(eq(loginRequest), any())).thenThrow(new UserNotVerifiedException(loginRequest.getEmail()));

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.title", is("User not verified")))
                .andExpect(jsonPath("$.detail", is("User with email " + loginRequest.getEmail() + " is not verified")))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    void shouldReturnVoidWhenVerifyUser() throws Exception {
        // given
        var verificationRequest = new VerificationRequest("mail@gmail.com", "123456");

        doNothing().when(authenticationService).verifyUser(eq(verificationRequest), any());


        mockMvc.perform(post("/auth/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(verificationRequest))
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorResponseVerifyUser() throws Exception {
        // given
        var verificationRequest = new VerificationRequest("mail@gmail.com", "123456");
        doThrow(new NoSuchUserByEmailException(verificationRequest.getEmail()))
                .when(authenticationService)
                .verifyUser(eq(verificationRequest), any());

        mockMvc.perform(post("/auth/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(verificationRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with email " + verificationRequest.getEmail())))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    void shouldReturnErrorResponseThrowUserAlreadyVerifiedExceptionWhenVerifyUser() throws Exception {
        // given
        var verificationRequest = new VerificationRequest("mail@gmail.com", "123456");

        doThrow(new UserAlreadyVerifiedException(verificationRequest.getEmail()))
                .when(authenticationService)
                .verifyUser(eq(verificationRequest), any());

        mockMvc.perform(post("/auth/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(verificationRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.title", is("User already verified")))
                .andExpect(jsonPath("$.detail", is("User with email " + verificationRequest.getEmail() + " is already verified")))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    void shouldReturnErrorResponseThrowVerificationCodeExpiredExceptionWhenVerifyUser() throws Exception {
        // given
        var verificationRequest = new VerificationRequest("mail@gmail.com", "123456");

        doThrow(new VerificationCodeExpiredException(verificationRequest.getVerificationCode()))
                .when(authenticationService)
                .verifyUser(eq(verificationRequest), any());

        mockMvc.perform(post("/auth/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(verificationRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.title", is("Verification code expired")))
                .andExpect(jsonPath("$.detail", is("Verification code expired: " + verificationRequest.getVerificationCode())))
                .andExpect(jsonPath("$.instance", is("/auth/verify")));
    }

    @Test
    void shouldReturnErrorResponseThrowInvalidVerificationCodeExceptionWhenVerifyUser() throws Exception {
        // given
        var verificationRequest = new VerificationRequest("mail@gmail.com", "123456");

        doThrow(new InvalidVerificationCodeException(verificationRequest.getEmail()))
                .when(authenticationService)
                .verifyUser(eq(verificationRequest), any());

        mockMvc.perform(post("/auth/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(verificationRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.title", is("Invalid verification code")))
                .andExpect(jsonPath("$.detail", is("Invalid verification code for " + verificationRequest.getEmail())))
                .andExpect(jsonPath("$.instance", is("/auth/verify")));
    }

    @Test
    void shouldReturnVoidWhenResendVerificationCode() throws Exception {
        // given
        var email = "mail@gmail.com";

        doNothing().when(authenticationService).resendVerificationCode(email);

        mockMvc.perform(post("/auth/resend")
                        .param("email", email)
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorResponseWhenResendVerificationCode() throws Exception {
        // given
        var email = "mail@gmail.com";

        doThrow(new NoSuchUserByEmailException(email))
                .when(authenticationService)
                .resendVerificationCode(email);

        mockMvc.perform(post("/auth/resend")
                        .param("email", email)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with email " + email)))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    void shouldReturnErrorResponseThrowUserAlreadyVerifiedExceptionWhenResendVerificationCode() throws Exception {
        // given
        var email = "mail@gmail.com";

        doThrow(new UserAlreadyVerifiedException(email))
                .when(authenticationService)
                .resendVerificationCode(email);

        mockMvc.perform(post("/auth/resend")
                        .param("email", email)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.title", is("User already verified")))
                .andExpect(jsonPath("$.detail", is("User with email " + email + " is already verified")))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    void shouldReturnAuthenticationResponseWhenRefreshToken() throws Exception {
        // given
        var refreshToken = "refresh_token";

        var authenticationResponse = AuthenticationResponse
                .builder()
                .accessToken("new_access_token")
                .refreshToken("new_refresh_token")
                .build();

        when(authenticationService.refreshToken(refreshToken)).thenReturn(authenticationResponse);

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", refreshToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", is(authenticationResponse.getAccessToken())))
                .andExpect(jsonPath("$.refresh_token", is(authenticationResponse.getRefreshToken())));
    }

    @Test
    void shouldReturnErrorResponseWhenRefreshTokenIsInvalid() throws Exception {
        // given
        var refreshToken = "refresh_token";

        when(authenticationService.refreshToken(refreshToken)).thenThrow(new InvalidTokenException(refreshToken));

        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", refreshToken)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.title", is("Invalid token")))
                .andExpect(jsonPath("$.detail", is("Invalid token: " + refreshToken)))
                .andExpect(jsonPath("$.instance", is("/auth/refresh")));
    }

    @Test
    void shouldSendPasswordReset() throws Exception {
        // given
        var passwordResetRequest = new PasswordResetRequest("mail@gmail.com");

        doNothing().when(authenticationService).sendPasswordReset(passwordResetRequest);

        mockMvc.perform(post("/auth/send-reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(passwordResetRequest))
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorResponseWhenUserNotFoundForPasswordReset() throws Exception {
        // given
        var passwordResetRequest = new PasswordResetRequest("mail@gmail.com");

        doThrow(new NoSuchUserByEmailException(passwordResetRequest.getEmail()))
                .when(authenticationService)
                .sendPasswordReset(passwordResetRequest);

        mockMvc.perform(post("/auth/send-reset-password")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(passwordResetRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such user")))
                .andExpect(jsonPath("$.detail", is("No user with email " + passwordResetRequest.getEmail())))
                .andExpect(jsonPath("$.instance", is("/users")));
    }

    @Test
    void shouldResetPasswordSuccessfully() throws Exception {
        // given
        var token = "valid_token";
        var newPassword = "new_password";

        var passwordResetConfirmRequest = new PasswordResetConfirmRequest(newPassword);
        doNothing().when(authenticationService).resetPassword(eq(token), eq(passwordResetConfirmRequest), any());

        // when
        mockMvc.perform(post("/auth/reset-password")
                        .param("token", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(passwordResetConfirmRequest))
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnErrorResponseWhenTokenNotFound() throws Exception {
        // given
        var token = "invalid_token";
        var newPassword = "new_password";
        var passwordResetConfirmRequest = new PasswordResetConfirmRequest(newPassword);

        doThrow(new NoSuchPasswordResetTokenException(token))
                .when(authenticationService)
                .resetPassword(eq(token), eq(passwordResetConfirmRequest), any());

        // when
        mockMvc.perform(post("/auth/reset-password")
                        .param("token", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(passwordResetConfirmRequest))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.title", is("No such password reset token")))
                .andExpect(jsonPath("$.detail", is("No password reset token with token: " + token)))
                .andExpect(jsonPath("$.instance", is("/auth/reset-password")));
    }

    @Test
    void shouldReturnErrorResponseWhenTokenExpired() throws Exception {
        // given
        var token = "invalid_token";
        var newPassword = "new_password";
        var passwordResetConfirmRequest = new PasswordResetConfirmRequest(newPassword);

        doThrow(new PasswordResetTokenExpiredException(token))
                .when(authenticationService)
                .resetPassword(eq(token), eq(passwordResetConfirmRequest), any());

        // when
        mockMvc.perform(post("/auth/reset-password")
                        .param("token", token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(passwordResetConfirmRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.title", is("Password reset token expired")))
                .andExpect(jsonPath("$.detail", is("Password reset token expired: " + token)))
                .andExpect(jsonPath("$.instance", is("/auth/reset-password")));
    }
}
