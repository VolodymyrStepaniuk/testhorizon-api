package com.stepaniuk.testhorizon.event.user;

import jakarta.annotation.Nullable;
import lombok.*;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserUpdatedEvent extends UserEvent {

    private final Data data;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "userId", "data"})
    public UserUpdatedEvent(Instant timestamp, String eventId, String correlationId,
                            Long userId, Data data) {
        super("UserUpdatedEvent", timestamp, eventId, correlationId, userId);
        this.data = data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class Data {

        @Nullable
        private String email;

        @Nullable
        private String firstName;

        @Nullable
        private String lastName;

    }
}
