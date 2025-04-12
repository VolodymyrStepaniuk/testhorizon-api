package com.stepaniuk.testhorizon.payload.feedback;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.validation.feedback.Comment;
import com.stepaniuk.testhorizon.validation.feedback.Rating;
import com.stepaniuk.testhorizon.validation.shared.Id;
import jakarta.annotation.Nullable;
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
@Relation(collectionRelation = "feedbacks", itemRelation = "feedbacks")
public class FeedbackResponse extends RepresentationModel<FeedbackResponse> {

    @Id
    @NotNull
    private Long id;

    @Rating
    @NotNull
    private Integer rating;

    @Comment
    @Nullable
    private String comment;

    @NotNull
    private UserInfo owner;

    @NotNull
    private final Instant createdAt;

    @NotNull
    private Instant updatedAt;
}
