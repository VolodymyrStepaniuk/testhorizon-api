package com.stepaniuk.testhorizon.payload.post;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import com.stepaniuk.testhorizon.validation.shared.Content;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Title;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "posts", itemRelation = "posts")
public class PostResponse extends RepresentationModel<PostResponse> {
    @Id
    @NotNull
    private final Long id;
    @NotNull
    private final UserInfo owner;
    @Title
    @NotNull
    private final String title;
    @Description
    @NotNull
    private final String description;
    @Content
    @NotNull
    private final String content;
    @NotNull
    private final PostCategoryName category;
    @NotNull
    private final Instant createdAt;
    @NotNull
    private final Instant updatedAt;
}
