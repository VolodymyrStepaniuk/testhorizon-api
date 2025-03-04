package com.stepaniuk.testhorizon.security.auth.passwordreset.exception;

import lombok.Getter;
/**
 * Exception thrown when passwords do not match
 */
@Getter
public class PasswordsDoNotMatchException extends RuntimeException {
    private final String password;
    private final String confirmPassword;

    public PasswordsDoNotMatchException(String password, String confirmPassword) {
        super("Password "+password+" and confirm password "+confirmPassword+" do not match");
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
}
