package com.stepaniuk.testhorizon.payload.comment;


import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.types.entity.EntityType;
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
    private final Long id;

    @NotNull
    private final EntityType entityType;

    @Id
    @NotNull
    private final Long entityId;

    @CommentContent
    @NotNull
    private final String content;

    @NotNull
    private final UserInfo author;

    @NotNull
    private final Instant createdAt;

    @NotNull
    private final Instant updatedAt;
}
