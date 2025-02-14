package com.stepaniuk.testhorizon.event.testcase;

import com.stepaniuk.testhorizon.types.testcase.TestCasePriorityName;
import jakarta.annotation.Nullable;
import lombok.*;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestCaseUpdatedEvent extends TestCaseEvent{

    private final Data data;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "testCaseId", "data"})
    public TestCaseUpdatedEvent(Instant timestamp, String eventId, String correlationId,
                                Long testCaseId, Data data) {
        super("TestCaseUpdatedEvent", timestamp, eventId, correlationId,testCaseId);
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
        private String preconditions;

        @Nullable
        private String inputData;

        @Nullable
        private List<String> steps;

        @Nullable
        private TestCasePriorityName  priority;

    }
}
