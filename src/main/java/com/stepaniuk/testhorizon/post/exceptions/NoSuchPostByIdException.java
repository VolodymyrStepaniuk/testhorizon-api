package com.stepaniuk.testhorizon.post.exceptions;

import lombok.Getter;
/*
    * This exception is thrown when a post with the specified ID does not exist.
 */
@Getter
public class NoSuchPostByIdException extends RuntimeException {
    private final Long postId;

    public NoSuchPostByIdException(Long postId) {
        super("Post with ID " + postId + " does not exist");
        this.postId = postId;
    }
}
