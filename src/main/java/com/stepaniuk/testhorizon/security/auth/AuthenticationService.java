package com.stepaniuk.testhorizon.security.auth;

import com.stepaniuk.testhorizon.payload.auth.AuthenticationResponse;
import com.stepaniuk.testhorizon.payload.auth.LoginRequest;
import com.stepaniuk.testhorizon.payload.auth.VerificationRequest;
import com.stepaniuk.testhorizon.payload.user.UserCreateRequest;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.security.EmailService;
import com.stepaniuk.testhorizon.security.JwtTokenService;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final EmailCodeRepository emailCodeRepository;
    private final JwtTokenService jwtTokenService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(UserCreateRequest request) {
        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
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

        newUser.setEmailCode(emailCode);

        newUser = userRepository.save(newUser);

        sendVerificationEmail(email, emailCode.getCode());

        return userMapper.toResponse(newUser);
    }

    public AuthenticationResponse authenticate(LoginRequest request) {
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

        return AuthenticationResponse.builder()
                .accessToken(jwtTokenService.generateAccessToken(user))
                .refreshToken(jwtTokenService.generateRefreshToken(user))
                .build();
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        return jwtTokenService.refreshToken(refreshToken);
    }

    @Transactional
    public void verifyUser(VerificationRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchUserByEmailException(email));

        if(user.isEnabled()) {
            throw new UserAlreadyVerifiedException(email);
        }

        if(user.getEmailCode().getExpiresAt().isBefore(Instant.now())) {
            throw new VerificationCodeExpiredException(request.getVerificationCode());
        }

        EmailCode emailCode = user.getEmailCode();

        if (emailCode.getCode().equals(request.getVerificationCode())) {
            user.setEnabled(true);
            user.setEmailCode(null);
            emailCodeRepository.deleteByCode(emailCode.getCode());
            userRepository.save(user);
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

        EmailCode emailCode = user.getEmailCode();
        emailCode.setCode(generateVerificationCode());
        emailCode.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

        sendVerificationEmail(email, emailCode.getCode());

        userRepository.save(user);
    }

    private void sendVerificationEmail(String email, String verificationCode) {
        String subject = "Account Verification";
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(email, subject, htmlMessage);
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
