package com.stepaniuk.testhorizon.user.exceptions;

import lombok.Getter;

/**
 * Exception thrown when user already exist with this email.
 *
 * @see RuntimeException
 */
@Getter
public class UserAlreadyExistsException extends RuntimeException {

    private final String email;

    public UserAlreadyExistsException(String email) {
        super("User already exists with email: " + email);
        this.email = email;
    }
}
