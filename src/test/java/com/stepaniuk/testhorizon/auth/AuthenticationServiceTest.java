package com.stepaniuk.testhorizon.auth;

import com.stepaniuk.testhorizon.event.auth.*;
import com.stepaniuk.testhorizon.payload.auth.*;
import com.stepaniuk.testhorizon.payload.user.UserCreateRequest;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.security.EmailService;
import com.stepaniuk.testhorizon.security.JwtTokenService;
import com.stepaniuk.testhorizon.security.auth.AuthProducer;
import com.stepaniuk.testhorizon.security.auth.AuthenticationService;
import com.stepaniuk.testhorizon.security.auth.passwordreset.PasswordResetToken;
import com.stepaniuk.testhorizon.security.auth.passwordreset.PasswordResetTokenRepository;
import com.stepaniuk.testhorizon.security.auth.passwordreset.exception.NoSuchPasswordResetTokenException;
import com.stepaniuk.testhorizon.security.auth.passwordreset.exception.PasswordResetTokenExpiredException;
import com.stepaniuk.testhorizon.security.exceptions.InvalidTokenException;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import com.stepaniuk.testhorizon.user.User;
import com.stepaniuk.testhorizon.user.UserMapperImpl;
import com.stepaniuk.testhorizon.user.UserRepository;
import com.stepaniuk.testhorizon.user.authority.Authority;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import com.stepaniuk.testhorizon.user.authority.AuthorityRepository;
import com.stepaniuk.testhorizon.user.email.EmailCode;
import com.stepaniuk.testhorizon.user.email.EmailCodeRepository;
import com.stepaniuk.testhorizon.user.email.exceptions.InvalidVerificationCodeException;
import com.stepaniuk.testhorizon.user.email.exceptions.VerificationCodeExpiredException;
import com.stepaniuk.testhorizon.user.exceptions.*;
import jakarta.mail.MessagingException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.stubbing.Answer1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {AuthenticationService.class, UserMapperImpl.class})
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @MockitoBean
    private AuthProducer authProducer;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthorityRepository authorityRepository;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private EmailCodeRepository emailCodeRepository;

    @MockitoBean
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldReturnUserResponseWhenRegisterUser() {
        // given
        UserCreateRequest request = new UserCreateRequest("existing.email@gmail.com", "password", "John", "Doe", AuthorityName.TESTER);
        Authority authority = new Authority(1L, request.getAuthorityName());

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(authorityRepository.findByName(request.getAuthorityName())).thenReturn(Optional.of(authority));
        when(userRepository.save(any())).thenAnswer(answer(getFakeSave(1L)));
        when(emailCodeRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        final var receivedEventWrapper = new UserRegisteredEvent[1];
        when(
                authProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (UserRegisteredEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        // when
        UserResponse response = authenticationService.register(request, UUID.randomUUID().toString());

        // then
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getFirstName(), response.getFirstName());
        assertEquals(request.getLastName(), response.getLastName());
        assertEquals(0, response.getTotalRating());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(response.getEmail(), receivedEvent.getEmail());

        verify(userRepository, times(1)).save(any());
        verify(emailCodeRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowUserAlreadyExistsExceptionWhenRegisterUser() {
        var correlationId = UUID.randomUUID().toString();
        UserCreateRequest request = new UserCreateRequest("existing.email@gmail.com", "password", "John", "Doe", AuthorityName.TESTER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(request, correlationId));
    }

    // Test case for missing authority
    @Test
    void shouldThrowNoSuchAuthorityExceptionWhenRegisterUser() {
        var correlationId = UUID.randomUUID().toString();
        UserCreateRequest request = new UserCreateRequest("existing.email@gmail.com", "password", "John", "Doe", AuthorityName.TESTER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(authorityRepository.findByName(request.getAuthorityName())).thenReturn(Optional.empty());

        assertThrows(NoSuchAuthorityException.class, () -> authenticationService.register(request, correlationId));
    }

    @Test
    void shouldReturnAuthenticationResponseWhenUserLogin() {
        LoginRequest request = new LoginRequest("existing.email@gmail.com", "password");
        User user = new User();
        user.setEmail(request.getEmail());
        user.setEnabled(true);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtTokenService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtTokenService.generateRefreshToken(user)).thenReturn("refresh-token");

        final var receivedEventWrapper = new UserAuthenticatedEvent[1];
        when(
                authProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (UserAuthenticatedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        AuthenticationResponse response = authenticationService.authenticate(request, UUID.randomUUID().toString());

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(user.getEmail(), receivedEvent.getEmail());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowNoSuchUserByEmailExceptionWhenUserLogin() {
        var correlationId = UUID.randomUUID().toString();
        LoginRequest request = new LoginRequest("nonexistent.email@gmail.com", "password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(NoSuchUserByEmailException.class, () -> authenticationService.authenticate(request, correlationId));
    }

    @Test
    void shouldThrowUserNotVerifiedExceptionWhenUserNotVerified() {
        var correlationId = UUID.randomUUID().toString();
        LoginRequest request = new LoginRequest("existing.email@gmail.com", "password");
        User user = new User();
        user.setEmail(request.getEmail());
        user.setEnabled(false);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        assertThrows(UserNotVerifiedException.class, () -> authenticationService.authenticate(request, correlationId));
    }

    @Test
    void shouldReturnAuthenticationResponseWhenRefreshTokenIsValid() {
        String refreshToken = "valid-refresh-token";
        AuthenticationResponse expectedResponse = AuthenticationResponse.builder()
                .accessToken("new_access_token")
                .refreshToken("new_refresh_token")
                .build();

        when(jwtTokenService.refreshToken(refreshToken)).thenReturn(expectedResponse);

        AuthenticationResponse response = authenticationService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals(expectedResponse.getAccessToken(), response.getAccessToken());
        assertEquals(expectedResponse.getRefreshToken(), response.getRefreshToken());

        verify(jwtTokenService, times(1)).refreshToken(refreshToken);
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsInvalid() {
        String refreshToken = "invalid-refresh-token";

        when(jwtTokenService.refreshToken(refreshToken)).thenThrow(new InvalidTokenException("Invalid refresh token"));

        assertThrows(InvalidTokenException.class, () -> authenticationService.refreshToken(refreshToken));

        verify(jwtTokenService, times(1)).refreshToken(refreshToken);
    }

    @Test
    void shouldVerifyUserSuccessfully() {
        VerificationRequest request = new VerificationRequest("existing.email@gmail.com", "123456");
        User user = new User();
        user.setEmail(request.getEmail());
        user.setEnabled(false);

        EmailCode emailCode = new EmailCode();
        emailCode.setCode("123456");
        emailCode.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(emailCodeRepository.findByCode(request.getVerificationCode())).thenReturn(Optional.of(emailCode));

        final var receivedEventWrapper = new UserVerifiedEvent[1];
        when(
                authProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (UserVerifiedEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        authenticationService.verifyUser(request, UUID.randomUUID().toString());

        assertTrue(user.isEnabled());

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(user.getEmail(), receivedEvent.getEmail());

        verify(userRepository, times(1)).save(user);
        verify(emailCodeRepository, times(1)).delete(emailCode);
    }

    @Test
    void shouldThrowNoSuchUserByEmailExceptionWhenVerifyUser() {
        var correlationId = UUID.randomUUID().toString();
        VerificationRequest request = new VerificationRequest("nonexistent.email@gmail.com", "123456");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(NoSuchUserByEmailException.class, () -> authenticationService.verifyUser(request, correlationId));
    }

    @Test
    void shouldThrowVerificationCodeExpiredExceptionWhenCodeIsExpired() {
        var correlationId = UUID.randomUUID().toString();
        VerificationRequest request = new VerificationRequest("existing.email@gmail.com", "123456");
        User user = new User();
        user.setEmail(request.getEmail());
        user.setEnabled(false);

        EmailCode emailCode = new EmailCode();
        emailCode.setCode("123456");
        emailCode.setExpiresAt(Instant.now().minus(10, ChronoUnit.MINUTES));
        emailCode.setUser(user);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(emailCodeRepository.findByCode(request.getVerificationCode())).thenReturn(Optional.of(emailCode));

        assertThrows(VerificationCodeExpiredException.class, () -> authenticationService.verifyUser(request, correlationId));
    }

    @Test
    void shouldThrowInvalidVerificationCodeExceptionWhenCodeIsInvalid() {
        var correlationId = UUID.randomUUID().toString();
        VerificationRequest request = new VerificationRequest("existing.email@gmail.com", "wrong-code");
        User user = new User();
        user.setEmail(request.getEmail());
        user.setEnabled(false);

        EmailCode emailCode = new EmailCode();
        emailCode.setCode("123456");
        emailCode.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        assertThrows(InvalidVerificationCodeException.class, () -> authenticationService.verifyUser(request, correlationId));
    }

    @Test
    void shouldResendVerificationCodeSuccessfully() throws MessagingException {
        String email = "existing.email@gmail.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(false);

        EmailCode emailCode = new EmailCode();
        emailCode.setCode("123456");
        emailCode.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        emailCode.setUser(user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(emailCodeRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());
        when(emailCodeRepository.findAllByUserId(user.getId())).thenReturn(List.of(new EmailCode(), new EmailCode()));

        authenticationService.resendVerificationCode(email);

        verify(emailService, times(1)).sendEmail(eq(email), anyString(), anyString());
        verify(emailCodeRepository, times(1)).deleteAll(any());
        verify(emailCodeRepository, times(1)).save(argThat(savedEmailCode ->
                savedEmailCode.getUser().equals(user) &&
                        savedEmailCode.getExpiresAt().isAfter(Instant.now())
        ));
    }

    @Test
    void shouldThrowNoSuchUserByEmailExceptionWhenResendVerificationCode() {
        String email = "nonexistent.email@gmail.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NoSuchUserByEmailException.class, () -> authenticationService.resendVerificationCode(email));
    }

    @Test
    void shouldThrowUserAlreadyVerifiedExceptionWhenResendVerificationCode() {
        String email = "existing.email@gmail.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyVerifiedException.class, () -> authenticationService.resendVerificationCode(email));
    }

    @Test
    void shouldReturnVoidWhenSendPasswordReset() throws Exception{
        String email = "existing.email@gmail.com";
        PasswordResetRequest request = new PasswordResetRequest(email);
        User user = new User();
        user.setEmail(email);
        user.setEnabled(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        authenticationService.sendPasswordReset(request);

        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendEmail(eq(email), anyString(), anyString());
    }

    @Test
    void shouldThrowNoSuchUserByEmailExceptionWhenSendPasswordReset() {
        String email = "nonexistent.email@gmail.com";
        PasswordResetRequest request = new PasswordResetRequest(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NoSuchUserByEmailException.class, () -> authenticationService.sendPasswordReset(request));
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        String token = "valid-token";
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("newPassword");
        String correlationId = UUID.randomUUID().toString();
        User user = new User();
        user.setEmail("existing.email@gmail.com");
        user.setEnabled(true);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        final var receivedEventWrapper = new UserResetPasswordEvent[1];
        when(
                authProducer.send(
                        assertArg(event -> receivedEventWrapper[0] = (UserResetPasswordEvent) event))).thenAnswer(
                answer(getFakeSendResult()
                )
        );

        authenticationService.resetPassword(token, request, correlationId);

        var receivedEvent = receivedEventWrapper[0];
        assertNotNull(receivedEvent);
        assertEquals(user.getEmail(), receivedEvent.getEmail());

        verify(userRepository, times(1)).save(user);
        verify(passwordResetTokenRepository, times(1)).delete(resetToken);
        verify(authProducer, times(1)).send(any(UserResetPasswordEvent.class));
    }

    @Test
    void shouldThrowNoSuchPasswordResetTokenExceptionWhenTokenNotFound() {
        String token = "invalid-token";
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("newPassword");
        String correlationId = UUID.randomUUID().toString();

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThrows(NoSuchPasswordResetTokenException.class, () -> authenticationService.resetPassword(token, request, correlationId));
    }

    @Test
    void shouldThrowPasswordResetTokenExpiredExceptionWhenTokenIsExpired() {
        String token = "expired-token";
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("newPassword");
        String correlationId = UUID.randomUUID().toString();
        User user = new User();
        user.setEmail("existing.email@gmail.com");
        user.setEnabled(true);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        assertThrows(PasswordResetTokenExpiredException.class, () -> authenticationService.resetPassword(token, request, correlationId));
    }

    private Answer1<User, User> getFakeSave(long id) {
        return user -> {
            user.setId(id);
            return user;
        };
    }

    private Answer1<CompletableFuture<SendResult<String, AuthEvent>>, AuthEvent> getFakeSendResult() {
        return event -> CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("auth", event),
                        new RecordMetadata(new TopicPartition("auth", 0), 0L, 0, 0L, 0, 0)));
    }
}
