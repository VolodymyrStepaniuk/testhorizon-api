package com.stepaniuk.testhorizon.event.feedback;

import java.beans.ConstructorProperties;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FeedbackUpdatedEvent extends FeedbackEvent{
    private final Data data;
    @ConstructorProperties({"timestamp", "eventId", "correlationId", "feedbackId","data"})
    public FeedbackUpdatedEvent(Instant timestamp, String eventId,
                                String correlationId, Long feedbackId,Data data) {
        super("FeedbackUpdatedEvent", timestamp, eventId, correlationId, feedbackId);
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
        private Integer rating;

        @Nullable
        private String comment;

    }
}
