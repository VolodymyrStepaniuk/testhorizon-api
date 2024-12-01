package com.stepaniuk.testhorizon.user.exceptions;

import lombok.Getter;

/**
 * Exception thrown when no user with given email exists.
 *
 * @see RuntimeException
 */
@Getter
public class NoSuchUserByEmailException extends RuntimeException{
    private final String email;

    public NoSuchUserByEmailException(String email) {
        super("No such user with email: " + email);
        this.email = email;
    }
}
