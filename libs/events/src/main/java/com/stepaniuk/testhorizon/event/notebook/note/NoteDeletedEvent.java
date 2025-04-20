package com.stepaniuk.testhorizon.event.notebook.note;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NoteDeletedEvent extends NoteEvent {

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "noteId"})
    public NoteDeletedEvent(Instant timestamp, String eventId, String correlationId,
                            Long noteId) {
        super("NoteDeletedEvent", timestamp, eventId, correlationId, noteId);
    }
}
