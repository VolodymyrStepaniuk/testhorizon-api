package com.stepaniuk.testhorizon.event.rating;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RatingUpdatedEvent extends RatingEvent{

    private final Integer ratingPoints;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "userId", "ratedByUserId", "ratingPoints"})
    public RatingUpdatedEvent(Instant timestamp, String eventId, String correlationId,
                              Long userId, Long ratedByUserId, Integer ratingPoints) {
        super("RatingUpdatedEvent", timestamp, eventId, correlationId, userId, ratedByUserId);
        this.ratingPoints = ratingPoints;
    }
}
