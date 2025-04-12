package com.stepaniuk.testhorizon.feedback.exceptions;

import lombok.Getter;
/*
 * Exception thrown when feedback with given id does not exist.
 */
@Getter
public class NoSuchFeedbackFoundByIdException extends RuntimeException {

    private final Long id;

    public NoSuchFeedbackFoundByIdException(Long id) {
        super("Feedback with id " + id + " does not exist");
        this.id = id;
    }
}
