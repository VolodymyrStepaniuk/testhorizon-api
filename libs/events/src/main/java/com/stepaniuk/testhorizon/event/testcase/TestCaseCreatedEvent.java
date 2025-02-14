package com.stepaniuk.testhorizon.event.testcase;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestCaseCreatedEvent extends TestCaseEvent {

    private final Long projectId;

    private final Long authorId;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "testCaseId", "projectId", "authorId"})
    public TestCaseCreatedEvent(Instant timestamp, String eventId, String correlationId,
                                Long testCaseId, Long projectId, Long authorId) {
        super("TestCaseCreatedEvent", timestamp, eventId, correlationId, testCaseId);
        this.projectId = projectId;
        this.authorId = authorId;
    }

}
