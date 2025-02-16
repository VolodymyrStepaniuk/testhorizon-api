package com.stepaniuk.testhorizon.event.project;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectDeletedEvent extends ProjectEvent {

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "projectId"})
    public ProjectDeletedEvent(Instant timestamp, String eventId, String correlationId,
                               Long projectId) {
        super("ProjectDeletedEvent", timestamp, eventId, correlationId, projectId);
    }

}
