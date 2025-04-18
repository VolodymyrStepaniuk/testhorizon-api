package com.stepaniuk.testhorizon.payload.comment;


import com.stepaniuk.testhorizon.types.entity.EntityType;
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
    private EntityType entityType;

    @Id
    @NotNull
    private Long entityId;

    @CommentContent
    @NotNull
    private String content;
}
