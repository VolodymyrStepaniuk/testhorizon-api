package com.stepaniuk.testhorizon.user.email.exceptions;

import lombok.Getter;

/**
 * Exception thrown when the verification code has expired.
 */
@Getter
public class VerificationCodeExpiredException extends RuntimeException {
    private final String code;

    public VerificationCodeExpiredException(String code) {
        super("Verification code expired: " + code);
        this.code = code;
    }
}
