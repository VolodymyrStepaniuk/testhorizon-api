package com.stepaniuk.testhorizon.event.comment;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CommentDeletedEvent extends CommentEvent {

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "commentId"})
    public CommentDeletedEvent(Instant timestamp, String eventId, String correlationId, Long commentId) {
        super("CommentDeletedEvent", timestamp, eventId, correlationId, commentId);
    }

}
