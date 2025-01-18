package com.stepaniuk.testhorizon.payload.comment.exception;

import lombok.Getter;

/**
 * Exception thrown when no comment with given id is found
 */
@Getter
public class NoSuchCommentByIdException extends RuntimeException {

    private final Long commentId;

    public NoSuchCommentByIdException(Long commentId) {
        super("No comment with id: " + commentId + " found");
        this.commentId = commentId;
    }
}
