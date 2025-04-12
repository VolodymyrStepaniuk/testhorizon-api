package com.stepaniuk.testhorizon.event.feedback;

import java.beans.ConstructorProperties;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FeedbackCreatedEvent extends FeedbackEvent{
    @ConstructorProperties({"timestamp", "eventId", "correlationId", "feedbackId"})
    public FeedbackCreatedEvent(Instant timestamp, String eventId,
                                String correlationId, Long feedbackId) {
        super("FeedbackCreateEvent", timestamp, eventId, correlationId, feedbackId);
    }
}
