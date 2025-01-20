package com.stepaniuk.testhorizon.payload.rating;

import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.user.Rating;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
@Relation(collectionRelation = "ratings", itemRelation = "ratings")
public class RatingResponse extends RepresentationModel<RatingResponse> {
    @Id
    @NotNull
    private final Long id;
    @Id
    @NotNull
    private final Long userId;
    @Id
    @NotNull
    private final Long ratedByUserId;
    @Rating
    @NotNull
    private final Integer ratingPoints;
    @Nullable
    private final String comment;
    @NotNull
    private final Instant createdAt;
}
