package com.stepaniuk.testhorizon.event.shared;

import java.time.Instant;

public interface ApplicationEvent {

    String getEventType();

    Instant getTimestamp();

    String getEventId();

    String getCorrelationId();

}
