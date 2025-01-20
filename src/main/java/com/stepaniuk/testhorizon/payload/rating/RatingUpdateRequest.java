package com.stepaniuk.testhorizon.payload.rating;

import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.user.Rating;
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
    @Id
    @Rating
    private final Integer ratingPoints;
    @Nullable
    private final String comment;
}
