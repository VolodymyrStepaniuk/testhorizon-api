package com.stepaniuk.testhorizon.event.project;

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
public class ProjectEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final Long projectId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "projectId"})
    public ProjectEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                        Long projectId) {
        super(eventType, timestamp, eventId, correlationId);
        this.projectId = projectId;
    }
}
