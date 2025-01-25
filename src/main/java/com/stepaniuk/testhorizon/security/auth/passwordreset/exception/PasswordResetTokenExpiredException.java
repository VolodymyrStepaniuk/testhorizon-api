package com.stepaniuk.testhorizon.security.auth.passwordreset.exception;

import lombok.Getter;

/**
 * Exception thrown when password reset token is expired
 */
@Getter
public class PasswordResetTokenExpiredException extends RuntimeException {

    private final String token;

    public PasswordResetTokenExpiredException(String token) {
        super("Password reset token: " + token + " is expired");
        this.token = token;
    }
}
