package com.stepaniuk.testhorizon.event.user;

import com.stepaniuk.testhorizon.types.user.AuthorityName;
import lombok.*;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserAuthorityUpdatedEvent extends UserEvent {

    private final AuthorityName authorityName;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "userId", "authorityName"})
    public UserAuthorityUpdatedEvent(Instant timestamp, String eventId, String correlationId,
                                     Long userId, AuthorityName authorityName) {
        super("UserAuthorityUpdatedEvent", timestamp, eventId, correlationId, userId);
        this.authorityName = authorityName;
    }
}
