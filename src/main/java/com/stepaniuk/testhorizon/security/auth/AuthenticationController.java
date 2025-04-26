package com.stepaniuk.testhorizon.security.auth;

import com.stepaniuk.testhorizon.payload.auth.*;
import com.stepaniuk.testhorizon.payload.auth.password.EmailPasswordResetConfirmRequest;
import com.stepaniuk.testhorizon.payload.auth.password.EmailPasswordResetRequest;
import com.stepaniuk.testhorizon.payload.auth.password.UpdatePasswordRequest;
import com.stepaniuk.testhorizon.payload.user.UserCreateRequest;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth", produces = "application/json")
@Validated
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping(path = "/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return ResponseEntity.ok(authenticationService.register(userCreateRequest, UUID.randomUUID().toString()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(loginRequest, UUID.randomUUID().toString()));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyUser(@Valid @RequestBody VerificationRequest verificationRequest) {
        authenticationService.verifyUser(verificationRequest, UUID.randomUUID().toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend")
    public ResponseEntity<Void> resendVerificationCode(@RequestParam String email) {
        authenticationService.resendVerificationCode(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        AuthenticationResponse response = authenticationService.refreshToken(refreshToken);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> sendEmailPasswordReset(@RequestBody EmailPasswordResetRequest request) {
        authenticationService.sendEmailPasswordReset(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> emailResetPassword(@RequestParam String token, @RequestBody EmailPasswordResetConfirmRequest request) {
        authenticationService.emailResetPassword(token, request, UUID.randomUUID().toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/update")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest request, AuthInfo authInfo) {
        authenticationService.updatePasswordAuthenticated(authInfo.getUserId(), request, UUID.randomUUID().toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/register-user")
    public ResponseEntity<UserResponse> registerUserByAdmin(@Valid @RequestBody UserCreateRequest userCreateRequest, AuthInfo authInfo) {
        return ResponseEntity.ok(authenticationService.registerUserByAdmin(userCreateRequest, authInfo, UUID.randomUUID().toString()));
    }


}
