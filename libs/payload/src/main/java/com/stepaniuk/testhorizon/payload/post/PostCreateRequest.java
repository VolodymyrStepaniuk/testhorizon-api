package com.stepaniuk.testhorizon.payload.post;

import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import com.stepaniuk.testhorizon.validation.shared.Content;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.Title;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PostCreateRequest {

    @Title
    @NotNull
    private String title;

    @Content
    @NotNull
    private String content;

    @Description
    @NotNull
    private String description;

    @NotNull
    private PostCategoryName category;
}
