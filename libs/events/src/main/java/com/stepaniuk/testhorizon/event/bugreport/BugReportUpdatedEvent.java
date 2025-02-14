package com.stepaniuk.testhorizon.event.bugreport;

import com.stepaniuk.testhorizon.types.bugreport.BugReportSeverityName;
import com.stepaniuk.testhorizon.types.bugreport.BugReportStatusName;
import jakarta.annotation.Nullable;
import lombok.*;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BugReportUpdatedEvent extends BugReportEvent {

    private final Data data;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "bugReportId", "data"})
    public BugReportUpdatedEvent(Instant timestamp, String eventId, String correlationId,
                                 Long bugReportId, Data data) {
        super("BugReportDeletedEvent", timestamp, eventId, correlationId, bugReportId);
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
        private String environment;

        @Nullable
        private BugReportSeverityName severity;

        @Nullable
        private BugReportStatusName status;
    }
}
