package com.stepaniuk.testhorizon.export.handler;

import com.stepaniuk.testhorizon.types.entity.EntityType;

public interface EntityExportHandler<T> {

    EntityType getEntityType();

    T findById(Long id);

    String toCsv(T obj);

    String toXml(T obj);
}

