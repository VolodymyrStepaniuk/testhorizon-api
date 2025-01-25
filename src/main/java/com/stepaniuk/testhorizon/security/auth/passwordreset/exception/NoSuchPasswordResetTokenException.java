package com.stepaniuk.testhorizon.security.auth.passwordreset.exception;

import lombok.Getter;

/**
 * Exception thrown when no password reset token is found in the database.
 */
@Getter
public class NoSuchPasswordResetTokenException extends RuntimeException {

    private final String token;

    public NoSuchPasswordResetTokenException(String token) {
        super("No such password reset token: " + token);
        this.token = token;
    }

}
