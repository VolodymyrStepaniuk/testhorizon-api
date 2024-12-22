package com.stepaniuk.testhorizon.test.exceptions;

import lombok.Getter;

/**
 * Exception thrown when test with given id does not exist.
 */
@Getter
public class NoSuchTestByIdException extends RuntimeException {
    private final Long id;

    public NoSuchTestByIdException(Long id) {
        super("Test with id " + id + " does not exist.");
        this.id = id;
    }
}
