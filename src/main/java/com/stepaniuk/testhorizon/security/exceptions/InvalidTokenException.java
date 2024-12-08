package com.stepaniuk.testhorizon.security.exceptions;

import lombok.Getter;

/**
 * Exception thrown when token is invalid.
 */
@Getter
public class InvalidTokenException extends RuntimeException {
    private final String token;
    public InvalidTokenException(String token) {
        super("Invalid token");
        this.token = token;
    }
}
