package com.stepaniuk.testhorizon.event.bugreport;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BugReportDeletedEvent extends BugReportEvent {

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "bugReportId"})
    public BugReportDeletedEvent(Instant timestamp, String eventId, String correlationId,
                                 Long bugReportId) {
        super("BugReportDeletedEvent", timestamp, eventId, correlationId, bugReportId);
    }

}
