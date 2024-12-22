package com.stepaniuk.testhorizon.security.auth;

import com.stepaniuk.testhorizon.payload.auth.AuthenticationResponse;
import com.stepaniuk.testhorizon.payload.auth.LoginRequest;
import com.stepaniuk.testhorizon.payload.auth.VerificationRequest;
import com.stepaniuk.testhorizon.payload.user.UserCreateRequest;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth", produces = "application/json")
@Validated
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping(path = "/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        return ResponseEntity.ok(authenticationService.register(userCreateRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(loginRequest));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyUser(@Valid @RequestBody VerificationRequest verificationRequest) {
        authenticationService.verifyUser(verificationRequest);
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
}
