package com.stepaniuk.testhorizon.event.notebook.note;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NoteCreatedEvent extends NoteEvent {

    private final Long notebookId;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "noteId", "notebookId"})
    public NoteCreatedEvent(Instant timestamp, String eventId, String correlationId,
                          Long noteId, Long notebookId) {
        super("NoteCreatedEvent", timestamp, eventId, correlationId, noteId);
        this.notebookId = notebookId;
    }
}
