package com.stepaniuk.testhorizon.payload.comment;

import com.stepaniuk.testhorizon.comment.type.CommentEntityType;
import com.stepaniuk.testhorizon.validation.comment.CommentContent;
import com.stepaniuk.testhorizon.validation.shared.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CommentCreateRequest {

    @NotNull
    private CommentEntityType entityType;

    @Id
    @NotNull
    private Long entityId;

    @NotNull
    @CommentContent
    private String content;
}
