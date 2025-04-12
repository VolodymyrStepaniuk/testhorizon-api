package com.stepaniuk.testhorizon.event.feedback;


import java.beans.ConstructorProperties;
import java.time.Instant;

import com.stepaniuk.testhorizon.event.shared.AbstractApplicationEvent;
import com.stepaniuk.testhorizon.event.shared.ApplicationEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FeedbackEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final Long feedbackId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "feedbackId"})
    public FeedbackEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                         Long feedbackId) {
        super(eventType, timestamp, eventId, correlationId);
        this.feedbackId = feedbackId;
    }
}
