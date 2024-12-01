package com.stepaniuk.testhorizon.user.exceptions;

import lombok.Getter;

/**
 * Exception thrown when user is already verified.
 *
 * @see RuntimeException
 */
@Getter
public class UserAlreadyVerifiedException extends RuntimeException {
    private final String email;

    public UserAlreadyVerifiedException(String email) {
        super("User is already verified with email: " + email);
        this.email = email;
    }
}
