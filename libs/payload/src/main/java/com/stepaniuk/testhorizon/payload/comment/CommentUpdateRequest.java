package com.stepaniuk.testhorizon.payload.comment;

import com.stepaniuk.testhorizon.validation.comment.CommentContent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CommentUpdateRequest {
    @CommentContent
    @NotNull
    private String content;
}
