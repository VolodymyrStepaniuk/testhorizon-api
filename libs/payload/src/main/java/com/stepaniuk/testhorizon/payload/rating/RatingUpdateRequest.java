package com.stepaniuk.testhorizon.payload.rating;

import com.stepaniuk.testhorizon.validation.rating.Comment;
import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Rating;
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
public class RatingUpdateRequest {
    @Id
    @NotNull
    private final Long userId;
    @Rating
    @NotNull
    private final Integer ratingPoints;
    @Comment
    @Nullable
    private final String comment;
}
