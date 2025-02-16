package com.stepaniuk.testhorizon.event.test;

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
public class TestEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final Long testId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "testId"})
    public TestEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                         Long testId) {
        super(eventType, timestamp, eventId, correlationId);
        this.testId = testId;
    }
}
