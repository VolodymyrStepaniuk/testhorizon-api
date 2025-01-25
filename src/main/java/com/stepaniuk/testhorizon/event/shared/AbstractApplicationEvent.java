package com.stepaniuk.testhorizon.event.shared;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class AbstractApplicationEvent implements ApplicationEvent {

    protected final String eventType;

    protected final Instant timestamp;

    protected final String eventId;

    protected final String correlationId;

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }
}
