package com.stepaniuk.testhorizon.user.exceptions;

import lombok.Getter;

/**
 * Exception thrown when user is not verified yet.
 *
 * @see RuntimeException
 */
@Getter
public class UserNotVerifiedException extends RuntimeException {

    private final String email;

    public UserNotVerifiedException(String email) {
        super("User with this email is not verified yet: " + email);
        this.email = email;
    }
}
