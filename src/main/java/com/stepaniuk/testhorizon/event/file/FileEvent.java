package com.stepaniuk.testhorizon.event.file;

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
public class FileEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final String fileName;
    private final String entityType;
    private final Long entityId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId",
            "fileName", "entityType", "entityId"})
    public FileEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                     String fileName, String entityType, Long entityId) {
        super(eventType, timestamp, eventId, correlationId);
        this.fileName = fileName;
        this.entityType = entityType;
        this.entityId = entityId;
    }
}
