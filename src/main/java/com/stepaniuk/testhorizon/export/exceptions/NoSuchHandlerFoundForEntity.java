package com.stepaniuk.testhorizon.export.exceptions;

import com.stepaniuk.testhorizon.types.entity.EntityType;
import lombok.Getter;

/**
 * Exception thrown when no such handler found for entity.
 */
@Getter
public class NoSuchHandlerFoundForEntity extends RuntimeException {

    private final EntityType entityType;

    public NoSuchHandlerFoundForEntity(EntityType entityType) {
        super("No handler found for entity type: " + entityType);
        this.entityType = entityType;
    }
}
