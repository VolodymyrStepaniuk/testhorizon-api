package com.stepaniuk.testhorizon.security.auth.passwordreset.exception;

import lombok.Getter;
/**
 * Exception thrown when passwords do not match
 */
@Getter
public class PasswordsDoNotMatchException extends RuntimeException {
    private final String firstTypeOfPassword;
    private final String secondTypeOfPassword;

    public PasswordsDoNotMatchException(String firstTypeOfPassword, String secondTypeOfPassword) {
        super("First password and second password do not match");
        this.firstTypeOfPassword = firstTypeOfPassword;
        this.secondTypeOfPassword = secondTypeOfPassword;
    }
}
