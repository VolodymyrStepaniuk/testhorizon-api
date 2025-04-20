package com.stepaniuk.testhorizon.event.notebook.note;

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
public class NoteEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final Long noteId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "noteId"})
    public NoteEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                        Long noteId) {
        super(eventType, timestamp, eventId, correlationId);
        this.noteId = noteId;
    }
}
