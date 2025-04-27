package com.stepaniuk.testhorizon.post.exceptions;

import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import lombok.Getter;

/**
 * Exception thrown when a post category with the specified name does not exist.
 */
@Getter
public class NoSuchPostCategoryByNameException extends RuntimeException {
    private final PostCategoryName postCategoryName;

    public NoSuchPostCategoryByNameException(final PostCategoryName postCategoryName) {
        super("Post category with name '" + postCategoryName + "' does not exist.");
        this.postCategoryName = postCategoryName;
    }
}
