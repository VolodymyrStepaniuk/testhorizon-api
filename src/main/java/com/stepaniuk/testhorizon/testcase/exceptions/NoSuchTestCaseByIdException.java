package com.stepaniuk.testhorizon.testcase.exceptions;

import lombok.Getter;

/**
 * Exception thrown when test case with given id is not found
 */
@Getter
public class NoSuchTestCaseByIdException extends RuntimeException{
    private final Long id;

    public NoSuchTestCaseByIdException(Long id) {
        super("Test case with id " + id + " not found");
        this.id = id;
    }
}
