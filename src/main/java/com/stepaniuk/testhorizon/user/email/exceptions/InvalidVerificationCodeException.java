package com.stepaniuk.testhorizon.user.email.exceptions;

import lombok.Getter;

/**
 * Exception thrown when verification code is invalid.
 *
 * @see RuntimeException
 */
@Getter
public class InvalidVerificationCodeException extends RuntimeException {
    private final String email;

    public InvalidVerificationCodeException(String email) {
        super("Invalid verification code for email: " + email);
        this.email = email;
    }
}
