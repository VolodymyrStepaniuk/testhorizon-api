package com.stepaniuk.testhorizon.payload.feedback;

import com.stepaniuk.testhorizon.validation.feedback.Comment;
import com.stepaniuk.testhorizon.validation.feedback.Rating;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.Nullable;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FeedbackCreateRequest {
    @Rating
    @NotNull
    private Integer rating;

    @Comment
    @Nullable
    private String comment;
}
