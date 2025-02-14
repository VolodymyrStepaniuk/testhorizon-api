package com.stepaniuk.testhorizon.event.auth;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserVerifiedEvent extends AuthEvent{

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "email"})
    public UserVerifiedEvent(Instant timestamp, String eventId, String correlationId, String email) {
        super("UserVerifiedEvent", timestamp, eventId, correlationId, email);
    }
}
