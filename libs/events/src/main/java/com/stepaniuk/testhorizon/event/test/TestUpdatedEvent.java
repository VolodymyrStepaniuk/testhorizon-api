package com.stepaniuk.testhorizon.event.test;

import com.stepaniuk.testhorizon.types.test.TestTypeName;
import jakarta.annotation.Nullable;
import lombok.*;

import java.beans.ConstructorProperties;
import java.time.Instant;


@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestUpdatedEvent extends TestEvent {

    private final Data data;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "testId", "data"})
    public TestUpdatedEvent(Instant timestamp, String eventId, String correlationId,
                            Long testId, Data data) {
        super("TestUpdatedEvent", timestamp, eventId, correlationId, testId);
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
        private Long testCaseId;

        @Nullable
        private String title;

        @Nullable
        private String description;

        @Nullable
        private String instructions;

        @Nullable
        private String githubUrl;

        @Nullable
        private TestTypeName type;

    }
}
