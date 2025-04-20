package com.stepaniuk.testhorizon.notebook.exceptions;

import lombok.Getter;

/**
 * Exception thrown when Notebook with given id does not exist.
 */
@Getter
public class NoSuchNotebookByIdException extends RuntimeException{

    private final Long id;

    public NoSuchNotebookByIdException(Long id) {
        super("Notebook with id " + id + " not found");
        this.id = id;
    }
}
