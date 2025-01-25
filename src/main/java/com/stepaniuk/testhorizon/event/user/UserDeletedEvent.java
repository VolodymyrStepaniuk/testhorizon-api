package com.stepaniuk.testhorizon.event.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserDeletedEvent extends UserEvent{

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "userId"})
    public UserDeletedEvent(Instant timestamp, String eventId, String correlationId,
                            Long userId) {
        super("UserDeleteEvent", timestamp, eventId, correlationId, userId);
    }
}
