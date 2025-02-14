package com.stepaniuk.testhorizon.event.testcase;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestCaseDeletedEvent extends TestCaseEvent{

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "testCaseId"})
    public TestCaseDeletedEvent(Instant timestamp, String eventId, String correlationId,
                                Long testCaseId) {
        super("TestCaseDeletedEvent", timestamp, eventId, correlationId, testCaseId);
    }

}
