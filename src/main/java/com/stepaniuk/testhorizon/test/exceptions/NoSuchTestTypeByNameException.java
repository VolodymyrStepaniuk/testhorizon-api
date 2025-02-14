package com.stepaniuk.testhorizon.test.exceptions;


import com.stepaniuk.testhorizon.types.test.TestTypeName;
import lombok.Getter;

/**
 * Exception thrown when test type with given name does not exist.
 */
@Getter
public class NoSuchTestTypeByNameException extends RuntimeException {
    private final TestTypeName name;

    public NoSuchTestTypeByNameException(TestTypeName name) {
        super("Test type with name " + name + " does not exist.");
        this.name = name;
    }
}
