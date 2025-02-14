package com.stepaniuk.testhorizon.event.project;

import com.stepaniuk.testhorizon.types.project.ProjectStatusName;
import jakarta.annotation.Nullable;
import lombok.*;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProjectUpdatedEvent extends ProjectEvent {

    private final Data data;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "projectId", "data"})
    public ProjectUpdatedEvent(Instant timestamp, String eventId, String correlationId,
                               Long projectId, Data data) {
        super("ProjectUpdatedEvent", timestamp, eventId, correlationId, projectId);
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

        @Nullable
        private ProjectStatusName status;

        @Nullable
        private String instructions;

    }
}
