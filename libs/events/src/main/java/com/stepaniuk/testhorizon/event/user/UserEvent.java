package com.stepaniuk.testhorizon.event.user;

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
public class UserEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final Long userId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "userId"})
    public UserEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                     Long userId) {
        super(eventType, timestamp, eventId, correlationId);
        this.userId = userId;
    }

}
