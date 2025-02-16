package com.stepaniuk.testhorizon.event.bugreport;

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
public class BugReportEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final Long bugReportId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "bugReportId"})
    public BugReportEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                          Long bugReportId) {
        super(eventType, timestamp, eventId, correlationId);
        this.bugReportId = bugReportId;
    }

}
