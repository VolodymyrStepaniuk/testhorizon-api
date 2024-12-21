package com.stepaniuk.testhorizon.bugreport.exceptions;

import lombok.Getter;

/**
 * Exception thrown when bug report with given id does not exist.
 */
@Getter
public class NoSuchBugReportByIdException extends RuntimeException{

    private final Long id;

    public NoSuchBugReportByIdException(Long id) {
        super("Bug report with id " + id + " does not exist");
        this.id = id;
    }
}
