package com.stepaniuk.testhorizon.event.feedback;

import java.beans.ConstructorProperties;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FeedbackDeletedEvent extends FeedbackEvent{
    @ConstructorProperties({"timestamp", "eventId", "correlationId", "feedbackId"})
    public FeedbackDeletedEvent(Instant timestamp, String eventId,
                                String correlationId, Long feedbackId) {
        super("FeedbackDeletedEvent", timestamp, eventId, correlationId, feedbackId);
    }
}
