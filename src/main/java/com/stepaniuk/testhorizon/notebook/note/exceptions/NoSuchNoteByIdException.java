package com.stepaniuk.testhorizon.notebook.note.exceptions;

import lombok.Getter;

/**
 * Exception thrown when a note with the specified ID does not exist.
 */
@Getter
public class NoSuchNoteByIdException extends RuntimeException {

    private final Long id;

    public NoSuchNoteByIdException(Long id) {
        super("Note with id " + id + " not found");
        this.id = id;
    }
}
