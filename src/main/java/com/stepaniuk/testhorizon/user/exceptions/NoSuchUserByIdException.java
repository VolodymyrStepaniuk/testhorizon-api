package com.stepaniuk.testhorizon.user.exceptions;

import lombok.Getter;

/**
 * Exception thrown when no user with given id exists.
 *
 * @see RuntimeException
 */
@Getter
public class NoSuchUserByIdException extends RuntimeException {

    private final Long id;

    public NoSuchUserByIdException(Long id) {
        super("No such user with id: " + id);
        this.id = id;
    }

}
