package com.stepaniuk.testhorizon.project.exception;

import lombok.Getter;

/**
 * Exception thrown when project with given id does not exist.
 */
@Getter
public class NoSuchProjectByIdException extends RuntimeException {
    private final Long id;

    public NoSuchProjectByIdException(Long id) {
        super("Project with id " + id + " does not exist");
        this.id = id;
    }
}
