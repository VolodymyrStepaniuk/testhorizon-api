package com.stepaniuk.testhorizon.event.project;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectCreatedEvent extends ProjectEvent{

    private final Long ownerId;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "projectId", "ownerId"})
    public ProjectCreatedEvent(Instant timestamp, String eventId, String correlationId,
                        Long projectId, Long ownerId) {
        super("ProjectCreatedEvent", timestamp, eventId, correlationId, projectId);
        this.ownerId = ownerId;
    }
}
