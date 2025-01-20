package com.stepaniuk.testhorizon.rating.exceptions;

import lombok.Getter;

/**
 * Exception thrown when user tries to change own rating.
 */
@Getter
public class UserCannotChangeOwnRatingException extends RuntimeException {

    private final Long userId;

    public UserCannotChangeOwnRatingException(Long userId) {
        super("User with id " + userId + " cannot change own rating.");
        this.userId = userId;
    }
}
