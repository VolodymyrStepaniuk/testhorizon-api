package com.stepaniuk.testhorizon.event.file;

import com.stepaniuk.testhorizon.types.files.FileEntityType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileUploadEvent extends FileEvent {

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId",
            "fileName", "entityType", "entityId"})
    public FileUploadEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                           String fileName, FileEntityType entityType, Long entityId) {
        super(eventType, timestamp, eventId, correlationId, fileName, entityType, entityId);
    }
}
