package com.stepaniuk.testhorizon.event.rating;

import com.stepaniuk.testhorizon.event.shared.AbstractApplicationEvent;
import com.stepaniuk.testhorizon.event.shared.ApplicationEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RatingEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final Long userId;
    private final Long ratedByUserId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "userId", "ratedByUserId"})
    public RatingEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                       Long userId, Long ratedByUserId) {
        super(eventType, timestamp, eventId, correlationId);
        this.userId = userId;
        this.ratedByUserId = ratedByUserId;
    }
}
