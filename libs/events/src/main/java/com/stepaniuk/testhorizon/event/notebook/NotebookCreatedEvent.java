package com.stepaniuk.testhorizon.event.notebook;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotebookCreatedEvent extends NotebookEvent {

    private final Long ownerId;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "notebookId", "ownerId"})
    public NotebookCreatedEvent(Instant timestamp, String eventId, String correlationId,
                             Long notebookId, Long ownerId) {
        super("NotebookCreatedEvent", timestamp, eventId, correlationId, notebookId);
        this.ownerId = ownerId;
    }
}
