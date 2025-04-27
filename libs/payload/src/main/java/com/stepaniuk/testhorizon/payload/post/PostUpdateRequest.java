package com.stepaniuk.testhorizon.payload.post;

import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import com.stepaniuk.testhorizon.validation.shared.Content;
import com.stepaniuk.testhorizon.validation.shared.Description;
import com.stepaniuk.testhorizon.validation.shared.Title;
import jakarta.annotation.Nullable;
import lombok.*;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PostUpdateRequest {
    @Title
    @Nullable
    private String title;

    @Description
    @Nullable
    private String description;

    @Content
    @Nullable
    private String content;

    @Nullable
    private PostCategoryName category;
}
