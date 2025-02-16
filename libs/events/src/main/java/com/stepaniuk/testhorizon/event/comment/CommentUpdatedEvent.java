package com.stepaniuk.testhorizon.event.comment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CommentUpdatedEvent extends CommentEvent{

    private final String content;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "commentId","content"})
    public CommentUpdatedEvent(Instant timestamp, String eventId, String correlationId, Long commentId, String content) {
        super("CommentUpdatedEvent", timestamp, eventId, correlationId, commentId);
        this.content = content;
    }
}
