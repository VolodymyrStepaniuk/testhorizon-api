package com.stepaniuk.testhorizon.auth;

import com.stepaniuk.testhorizon.payload.auth.AuthenticationResponse;
import com.stepaniuk.testhorizon.payload.auth.LoginRequest;
import com.stepaniuk.testhorizon.payload.auth.VerificationRequest;
import com.stepaniuk.testhorizon.payload.user.UserCreateRequest;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.security.EmailService;
import com.stepaniuk.testhorizon.security.JwtTokenService;
import com.stepaniuk.testhorizon.security.auth.AuthenticationService;
import com.stepaniuk.testhorizon.security.exceptions.InvalidTokenException;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import com.stepaniuk.testhorizon.user.User;
import com.stepaniuk.testhorizon.user.UserMapperImpl;
import com.stepaniuk.testhorizon.user.UserRepository;
import com.stepaniuk.testhorizon.user.authority.Authority;
import com.stepaniuk.testhorizon.user.authority.AuthorityName;
import com.stepaniuk.testhorizon.user.authority.AuthorityRepository;
import com.stepaniuk.testhorizon.user.email.EmailCode;
import com.stepaniuk.testhorizon.user.email.EmailCodeRepository;
import com.stepaniuk.testhorizon.user.email.exceptions.InvalidVerificationCodeException;
import com.stepaniuk.testhorizon.user.email.exceptions.VerificationCodeExpiredException;
import com.stepaniuk.testhorizon.user.exceptions.*;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {AuthenticationService.class, UserMapperImpl.class})
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

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
        when(userRepository.save(any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        // when
        UserResponse response = authenticationService.register(request);

        // then
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getFirstName(), response.getFirstName());
        assertEquals(request.getLastName(), response.getLastName());
        assertEquals(0, response.getTotalRating());

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowUserAlreadyExistsExceptionWhenRegisterUser() {
        UserCreateRequest request = new UserCreateRequest("existing.email@gmail.com", "password", "John", "Doe", AuthorityName.TESTER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(request));
    }

    // Test case for missing authority
    @Test
    void shouldThrowNoSuchAuthorityExceptionWhenRegisterUser() {
        UserCreateRequest request = new UserCreateRequest("existing.email@gmail.com", "password", "John", "Doe", AuthorityName.TESTER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(authorityRepository.findByName(request.getAuthorityName())).thenReturn(Optional.empty());

        assertThrows(NoSuchAuthorityException.class, () -> authenticationService.register(request));
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

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowNoSuchUserByEmailExceptionWhenUserLogin() {
        LoginRequest request = new LoginRequest("nonexistent.email@gmail.com", "password");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(NoSuchUserByEmailException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void shouldThrowUserNotVerifiedExceptionWhenUserNotVerified() {
        LoginRequest request = new LoginRequest("existing.email@gmail.com", "password");
        User user = new User();
        user.setEmail(request.getEmail());
        user.setEnabled(false);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        assertThrows(UserNotVerifiedException.class, () -> authenticationService.authenticate(request));
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
        user.setEmailCode(emailCode);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        authenticationService.verifyUser(request);

        assertTrue(user.isEnabled());
        assertNull(user.getEmailCode());
        verify(emailCodeRepository, times(1)).deleteByCode(emailCode.getCode());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldThrowNoSuchUserByEmailExceptionWhenVerifyUser() {
        VerificationRequest request = new VerificationRequest("nonexistent.email@gmail.com", "123456");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(NoSuchUserByEmailException.class, () -> authenticationService.verifyUser(request));
    }

    @Test
    void shouldThrowVerificationCodeExpiredExceptionWhenCodeIsExpired() {
        VerificationRequest request = new VerificationRequest("existing.email@gmail.com", "123456");
        User user = new User();
        user.setEmail(request.getEmail());
        user.setEnabled(false);

        EmailCode emailCode = new EmailCode();
        emailCode.setCode("123456");
        emailCode.setExpiresAt(Instant.now().minus(10, ChronoUnit.MINUTES));
        user.setEmailCode(emailCode);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        assertThrows(VerificationCodeExpiredException.class, () -> authenticationService.verifyUser(request));
    }

    @Test
    void shouldThrowInvalidVerificationCodeExceptionWhenCodeIsInvalid() {
        VerificationRequest request = new VerificationRequest("existing.email@gmail.com", "wrong-code");
        User user = new User();
        user.setEmail(request.getEmail());
        user.setEnabled(false);

        EmailCode emailCode = new EmailCode();
        emailCode.setCode("123456");
        emailCode.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        user.setEmailCode(emailCode);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        assertThrows(InvalidVerificationCodeException.class, () -> authenticationService.verifyUser(request));
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
        user.setEmailCode(emailCode);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        authenticationService.resendVerificationCode(email);

        assertNotEquals("123456", emailCode.getCode());
        assertTrue(emailCode.getExpiresAt().isAfter(Instant.now()));
        verify(emailService, times(1)).sendVerificationEmail(eq(email), anyString(), anyString());
        verify(userRepository, times(1)).save(user);
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
}
