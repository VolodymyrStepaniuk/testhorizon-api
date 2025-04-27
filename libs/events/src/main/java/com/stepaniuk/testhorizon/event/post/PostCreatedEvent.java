package com.stepaniuk.testhorizon.event.post;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PostCreatedEvent extends PostEvent {
    private final Long ownerId;

    public PostCreatedEvent(Instant timestamp, String eventId, String correlationId,
                            Long postId, Long ownerId) {
        super("PostCreatedEvent", timestamp, eventId, correlationId, postId);
        this.ownerId = ownerId;
    }
}
