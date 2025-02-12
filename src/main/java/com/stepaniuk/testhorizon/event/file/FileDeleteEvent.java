package com.stepaniuk.testhorizon.event.file;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileDeleteEvent extends FileEvent {

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId",
            "fileName", "entityType", "entityId"})
    public FileDeleteEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                           String fileName, String entityType, Long entityId) {
        super(eventType, timestamp, eventId, correlationId, fileName, entityType, entityId);
    }
}
