package com.stepaniuk.testhorizon.event.notebook.note;

import jakarta.annotation.Nullable;
import lombok.*;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NoteUpdatedEvent extends NoteEvent {

    private final Data data;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "noteId", "data"})
    public NoteUpdatedEvent(Instant timestamp, String eventId, String correlationId,
                            Long noteId, Data data) {
        super("NoteUpdatedEvent", timestamp, eventId, correlationId, noteId);
        this.data = data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class Data {

        @Nullable
        private String title;

        @Nullable
        private String content;
    }
}
