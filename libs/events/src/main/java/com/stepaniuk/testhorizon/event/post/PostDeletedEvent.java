package com.stepaniuk.testhorizon.event.post;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PostDeletedEvent extends PostEvent {

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "postId"})
    public PostDeletedEvent(Instant timestamp, String eventId, String correlationId,
                               Long postId) {
        super("PostDeletedEvent", timestamp, eventId, correlationId, postId);
    }
}
