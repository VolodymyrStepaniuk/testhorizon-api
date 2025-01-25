package com.stepaniuk.testhorizon.event.test;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestDeletedEvent extends TestEvent {

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "testId"})
    public TestDeletedEvent(Instant timestamp, String eventId, String correlationId,
                            Long testId) {
        super("TestDeletedEvent", timestamp, eventId, correlationId, testId);
    }

}
