package com.stepaniuk.testhorizon.testcase.exceptions;

import com.stepaniuk.testhorizon.testcase.priority.TestCasePriorityName;
import lombok.Getter;

/**
 * Exception thrown when priority with given name is not found
 */
@Getter
public class NoSuchTestCasePriorityByNameException extends RuntimeException {
    private final TestCasePriorityName name;

    public NoSuchTestCasePriorityByNameException(TestCasePriorityName name) {
        super("Priority with name " + name + " not found");
        this.name = name;
    }
}