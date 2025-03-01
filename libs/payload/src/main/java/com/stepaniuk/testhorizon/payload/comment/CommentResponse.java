package com.stepaniuk.testhorizon.payload.comment;


import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.types.comment.CommentEntityType;
import com.stepaniuk.testhorizon.validation.comment.CommentContent;
import com.stepaniuk.testhorizon.validation.shared.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "comments", itemRelation = "comments")
public class CommentResponse extends RepresentationModel<CommentResponse> {
    @Id
    @NotNull
    private Long id;

    @NotNull
    private CommentEntityType entityType;

    @Id
    @NotNull
    private Long entityId;

    @CommentContent
    @NotNull
    private String content;

    @NotNull
    private UserInfo author;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
