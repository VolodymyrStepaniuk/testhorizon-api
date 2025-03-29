package com.stepaniuk.testhorizon.security.exceptions;

import lombok.Getter;
/**
 * Exception thrown when passwords do not match
 */
@Getter
public class PasswordsDoNotMatchException extends RuntimeException {

    public PasswordsDoNotMatchException() {
        super("New password and confirmation password do not match");
    }
}
