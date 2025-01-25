package com.stepaniuk.testhorizon.event.bugreport;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BugReportCreatedEvent extends BugReportEvent {

    private final Long projectId;
    private final Long reporterId;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "bugReportId", "projectId", "reporterId"})
    public BugReportCreatedEvent(Instant timestamp, String eventId, String correlationId,
                                 Long bugReportId, Long projectId, Long reporterId) {
        super("BugReportCreatedEvent", timestamp, eventId, correlationId, bugReportId);
        this.projectId = projectId;
        this.reporterId = reporterId;
    }

}
