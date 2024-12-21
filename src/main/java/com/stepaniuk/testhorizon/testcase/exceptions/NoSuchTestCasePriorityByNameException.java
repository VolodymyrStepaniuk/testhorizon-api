package com.stepaniuk.testhorizon.testcase.exceptions;

import lombok.Getter;

/**
 * Exception thrown when priority with given name is not found
 */
@Getter
public class NoSuchTestCasePriorityByNameException extends RuntimeException {
    private final String name;

    public NoSuchTestCasePriorityByNameException(String name) {
        super("Priority with name " + name + " not found");
        this.name = name;
    }
}