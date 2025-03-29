package com.stepaniuk.testhorizon.security.exceptions;

import lombok.Getter;

/**
 * Exception thrown when the old password provided during a password update is invalid.
 * This exception is typically used to indicate that the user has entered an incorrect
 * old password when attempting to change their password.
 */
@Getter
public class InvalidOldPasswordException extends RuntimeException{

    public InvalidOldPasswordException() {
        super("The old password provided is invalid.");
    }

}
