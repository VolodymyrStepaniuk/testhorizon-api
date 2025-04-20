package com.stepaniuk.testhorizon.event.notebook;

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
public class NotebookEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final Long notebookId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "notebookId"})
    public NotebookEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                     Long notebookId) {
        super(eventType, timestamp, eventId, correlationId);
        this.notebookId = notebookId;
    }
}
