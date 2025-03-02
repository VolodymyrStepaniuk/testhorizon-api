package com.stepaniuk.testhorizon.export;

import com.stepaniuk.testhorizon.export.exceptions.NoSuchHandlerFoundForEntity;
import com.stepaniuk.testhorizon.export.handler.EntityExportHandler;
import com.stepaniuk.testhorizon.types.entity.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EntityExportService {

    private final Map<EntityType, EntityExportHandler<?>> handlerMap = new ConcurrentHashMap<>();

    @Autowired
    public EntityExportService(List<EntityExportHandler<?>> handlers) {
        for (EntityExportHandler<?> handler : handlers) {
            handlerMap.put(handler.getEntityType(), handler);
        }
    }

    public String exportCsv(EntityType entityType, Long id) {
        EntityExportHandler<?> handler = handlerMap.get(entityType);

        if (handler == null) {
            throw new NoSuchHandlerFoundForEntity(entityType);
        }

        Object obj = handler.findById(id);

        @SuppressWarnings("unchecked")
        EntityExportHandler<Object> casted = (EntityExportHandler<Object>) handler;

        return casted.toCsv(obj);
    }

    public String exportXml(EntityType entityType, Long id) {
        EntityExportHandler<?> handler = handlerMap.get(entityType);

        if (handler == null) {
            throw new NoSuchHandlerFoundForEntity(entityType);
        }

        Object obj = handler.findById(id);

        @SuppressWarnings("unchecked")
        EntityExportHandler<Object> casted = (EntityExportHandler<Object>) handler;

        return casted.toXml(obj);
    }
}
