package com.stepaniuk.testhorizon.event.notebook;

import jakarta.annotation.Nullable;
import lombok.*;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NotebookUpdatedEvent extends NotebookEvent {

    private final Data data;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "notebookId", "data"})
    public NotebookUpdatedEvent(Instant timestamp, String eventId, String correlationId,
                               Long notebookId, Data data) {
        super("NotebookUpdatedEvent", timestamp, eventId, correlationId, notebookId);
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
        private String description;
    }
}
