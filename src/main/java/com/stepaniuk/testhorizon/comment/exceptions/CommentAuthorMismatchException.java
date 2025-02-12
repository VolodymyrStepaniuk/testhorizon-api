package com.stepaniuk.testhorizon.comment.exceptions;

import lombok.Getter;

/**
 * Exception thrown when comment author does not match the user
 */
@Getter
public class CommentAuthorMismatchException extends RuntimeException {

    private final Long commentId;
    private final Long userId;

    public CommentAuthorMismatchException(Long commentId, Long userId) {
        super("Comment with id: " + commentId + " does not belong to user with id: " + userId);
        this.commentId = commentId;
        this.userId = userId;
    }
}
