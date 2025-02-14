package com.stepaniuk.testhorizon.event.auth;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserAuthenticatedEvent extends AuthEvent {

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "email"})
    public UserAuthenticatedEvent(Instant timestamp, String eventId, String correlationId, String email) {
        super("UserAuthenticatedEvent", timestamp, eventId, correlationId, email);
    }
}
