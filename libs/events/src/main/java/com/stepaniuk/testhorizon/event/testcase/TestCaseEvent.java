package com.stepaniuk.testhorizon.event.testcase;

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
public class TestCaseEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final Long testCaseId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "testCaseId"})
    public TestCaseEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                         Long testCaseId) {
        super(eventType, timestamp, eventId, correlationId);
        this.testCaseId = testCaseId;
    }

}
