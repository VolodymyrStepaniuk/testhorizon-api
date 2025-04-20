package com.stepaniuk.testhorizon.event.notebook;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotebookDeletedEvent extends NotebookEvent {

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "notebookId"})
    public NotebookDeletedEvent(Instant timestamp, String eventId, String correlationId,
                               Long notebookId) {
        super("NotebookDeletedEvent", timestamp, eventId, correlationId, notebookId);
    }
}
