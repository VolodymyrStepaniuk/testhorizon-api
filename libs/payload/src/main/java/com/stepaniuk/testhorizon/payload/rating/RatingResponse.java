package com.stepaniuk.testhorizon.payload.rating;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.validation.rating.Comment;
import com.stepaniuk.testhorizon.validation.shared.Id;
import com.stepaniuk.testhorizon.validation.shared.Rating;
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
    @NotNull
    private final UserInfo user;
    @NotNull
    private final UserInfo ratedByUser;
    @Rating
    @NotNull
    private final Integer ratingPoints;
    @Comment
    @Nullable
    private final String comment;
    @NotNull
    private final Instant createdAt;
}
