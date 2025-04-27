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
    private Long id;
    @NotNull
    private UserInfo owner;
    @Title
    @NotNull
    private String title;
    @Description
    @NotNull
    private String description;
    @Content
    @NotNull
    private String content;
    @NotNull
    private PostCategoryName category;
    @NotNull
    private Instant createdAt;
    @NotNull
    private Instant updatedAt;
}
