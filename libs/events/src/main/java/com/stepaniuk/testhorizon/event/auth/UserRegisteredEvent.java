package com.stepaniuk.testhorizon.event.auth;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserRegisteredEvent extends AuthEvent {

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "email"})
    public UserRegisteredEvent(Instant timestamp, String eventId, String correlationId, String email) {
        super("UserRegisteredEvent", timestamp, eventId, correlationId, email);
    }

}
