package com.stepaniuk.testhorizon.security.auth;

import com.stepaniuk.testhorizon.event.auth.UserAuthenticatedEvent;
import com.stepaniuk.testhorizon.event.auth.UserRegisteredEvent;
import com.stepaniuk.testhorizon.event.auth.UserResetPasswordEvent;
import com.stepaniuk.testhorizon.event.auth.UserVerifiedEvent;
import com.stepaniuk.testhorizon.payload.auth.*;
import com.stepaniuk.testhorizon.payload.auth.password.EmailPasswordResetConfirmRequest;
import com.stepaniuk.testhorizon.payload.auth.password.EmailPasswordResetRequest;
import com.stepaniuk.testhorizon.payload.auth.password.UpdatePasswordRequest;
import com.stepaniuk.testhorizon.payload.user.UserCreateRequest;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.security.EmailService;
import com.stepaniuk.testhorizon.security.JwtTokenService;
import com.stepaniuk.testhorizon.security.auth.passwordreset.PasswordResetToken;
import com.stepaniuk.testhorizon.security.auth.passwordreset.PasswordResetTokenRepository;
import com.stepaniuk.testhorizon.security.auth.passwordreset.exception.NoSuchPasswordResetTokenException;
import com.stepaniuk.testhorizon.security.auth.passwordreset.exception.PasswordResetTokenExpiredException;
import com.stepaniuk.testhorizon.security.auth.passwordreset.exception.PasswordsDoNotMatchException;
import com.stepaniuk.testhorizon.user.User;
import com.stepaniuk.testhorizon.user.UserMapper;
import com.stepaniuk.testhorizon.user.UserRepository;
import com.stepaniuk.testhorizon.user.authority.AuthorityRepository;
import com.stepaniuk.testhorizon.user.email.EmailCode;
import com.stepaniuk.testhorizon.user.email.EmailCodeRepository;
import com.stepaniuk.testhorizon.user.email.exceptions.InvalidVerificationCodeException;
import com.stepaniuk.testhorizon.user.email.exceptions.VerificationCodeExpiredException;
import com.stepaniuk.testhorizon.user.exceptions.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.stepaniuk.testhorizon.shared.EmailTemplateUtility.loadEmailTemplate;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final EmailCodeRepository emailCodeRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthProducer authProducer;

    public UserResponse register(UserCreateRequest request, String correlationId) {
        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setTotalRating(0);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEnabled(false);
        newUser.setAccountNonExpired(true);
        newUser.setAccountNonLocked(true);
        newUser.setCredentialsNonExpired(true);

        var authorityName = request.getAuthorityName();

        var userAuthority = authorityRepository.findByName(authorityName)
                .orElseThrow(() -> new NoSuchAuthorityException(authorityName));

        var authorities = new HashSet<>(Set.of(userAuthority));

        newUser.setAuthorities(authorities);

        EmailCode emailCode = new EmailCode();
        emailCode.setUser(newUser);
        emailCode.setCode(generateVerificationCode());
        emailCode.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));

        var savedUser = userRepository.save(newUser);
        var savedEmailCode = emailCodeRepository.save(emailCode);

        authProducer.send(
                new UserRegisteredEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        newUser.getEmail()
                )
        );

        sendVerificationEmail(email, savedEmailCode.getCode());

        return userMapper.toResponse(savedUser, null);
    }

    public AuthenticationResponse authenticate(LoginRequest request, String correlationId) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchUserByEmailException(email));

        if (!user.isEnabled()) {
            throw new UserNotVerifiedException(email);
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email, request.getPassword()
                )
        );

        authProducer.send(
                new UserAuthenticatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        user.getEmail()
                )
        );

        return AuthenticationResponse.builder()
                .accessToken(jwtTokenService.generateAccessToken(user))
                .refreshToken(jwtTokenService.generateRefreshToken(user))
                .build();
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        return jwtTokenService.refreshToken(refreshToken);
    }

    public void verifyUser(VerificationRequest request, String correlationId) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchUserByEmailException(email));

        EmailCode emailCode = emailCodeRepository.findByCode(request.getVerificationCode())
                .orElseThrow(() -> new InvalidVerificationCodeException(email));

        if (user.isEnabled()) {
            throw new UserAlreadyVerifiedException(email);
        }

        if (emailCode.getExpiresAt().isBefore(Instant.now())) {
            throw new VerificationCodeExpiredException(request.getVerificationCode());
        }

        if (emailCode.getCode().equals(request.getVerificationCode())) {
            user.setEnabled(true);
            userRepository.save(user);

            emailCodeRepository.delete(emailCode);

            authProducer.send(
                    new UserVerifiedEvent(
                            Instant.now(), UUID.randomUUID().toString(), correlationId,
                            user.getEmail()
                    )
            );
        } else {
            throw new InvalidVerificationCodeException(email);
        }
    }

    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchUserByEmailException(email));

        if (user.isEnabled()) {
            throw new UserAlreadyVerifiedException(email);
        }

        var emailCodes = emailCodeRepository.findAllByUserId(user.getId());

        if (!emailCodes.isEmpty()) {
            emailCodeRepository.deleteAll(emailCodes);
        }

        EmailCode emailCode = new EmailCode();
        emailCode.setCode(generateVerificationCode());
        emailCode.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        emailCode.setUser(user);

        var savedEmailCode = emailCodeRepository.save(emailCode);

        sendVerificationEmail(email, savedEmailCode.getCode());
    }

    public void sendEmailPasswordReset(EmailPasswordResetRequest emailPasswordResetRequest) {
        String email = emailPasswordResetRequest.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchUserByEmailException(email));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

        passwordResetTokenRepository.save(resetToken);

        sendPasswordResetEmail(email, token);
    }

    public void emailResetPassword(String token, EmailPasswordResetConfirmRequest request, String correlationId) {
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordsDoNotMatchException("newPassword", "confirmPassword");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new NoSuchPasswordResetTokenException(token));

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new PasswordResetTokenExpiredException(token);
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        authProducer.send(
                new UserResetPasswordEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        user.getEmail()
                )
        );

        passwordResetTokenRepository.delete(resetToken);
    }

    public void updatePasswordAuthenticated(Long id, UpdatePasswordRequest request, String correlationId) {
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            throw new PasswordsDoNotMatchException("newPassword", "confirmPassword");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchUserByIdException(id));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new PasswordsDoNotMatchException("oldPassword","oldPasswordFromRequest");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        authProducer.send(
                new UserResetPasswordEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        user.getEmail()
                )
        );
    }

    private void sendVerificationEmail(String email, String verificationCode) {
        String subject = "Account Verification";
        String verificationTemplate = loadEmailTemplate(
                "verification-email-template.html",
                Map.of("verificationCode", verificationCode)
        );

        try {
            emailService.sendEmail(email, subject, verificationTemplate);
        } catch (MessagingException e) {
            // Handle email sending exception
            e.printStackTrace();
        }
    }

    private void sendPasswordResetEmail(String email, String token) {
        String subject = "Password Reset";
        String htmlMessage = loadEmailTemplate(
                "password-reset-template.html",
                Map.of("resetToken", token)
        );

        try {
            emailService.sendEmail(email, subject, htmlMessage);
        } catch (MessagingException e) {
            // Handle email sending exception
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
