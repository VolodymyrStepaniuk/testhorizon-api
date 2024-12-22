package com.stepaniuk.testhorizon.test.exceptions;

import lombok.Getter;

/**
 * Exception thrown when test type with given name does not exist.
 */
@Getter
public class NoSuchTestTypeByNameException extends RuntimeException {
    private final String name;

    public NoSuchTestTypeByNameException(String name) {
        super("Test type with name " + name + " does not exist.");
        this.name = name;
    }
}
