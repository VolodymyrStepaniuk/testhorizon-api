package com.stepaniuk.testhorizon.event.auth;

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
public class AuthEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final String email;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "email"})
    public AuthEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                     String email) {
        super(eventType, timestamp, eventId, correlationId);
        this.email = email;
    }
}
