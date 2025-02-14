package com.stepaniuk.testhorizon.event.test;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestCreatedEvent extends TestEvent {

    private final Long projectId;

    private final Long authorId;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "testId", "projectId", "authorId"})
    public TestCreatedEvent(Instant timestamp, String eventId, String correlationId,
                            Long testId, Long projectId, Long authorId) {
        super("TestCreatedEvent", timestamp, eventId, correlationId, testId);
        this.projectId = projectId;
        this.authorId = authorId;
    }

}
