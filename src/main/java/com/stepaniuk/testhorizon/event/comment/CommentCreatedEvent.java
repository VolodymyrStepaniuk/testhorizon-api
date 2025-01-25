package com.stepaniuk.testhorizon.event.comment;

import com.stepaniuk.testhorizon.comment.type.CommentEntityType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CommentCreatedEvent extends CommentEvent {

    private final Long authorId;

    private final CommentEntityType entityType;

    private final Long entityId;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "commentId", "authorId", "entityType", "entityId"})
    public CommentCreatedEvent(Instant timestamp, String eventId, String correlationId,
                               Long commentId, Long authorId, CommentEntityType entityType, Long entityId) {
        super("CommentCreatedEvent", timestamp, eventId, correlationId, commentId);
        this.authorId = authorId;
        this.entityType = entityType;
        this.entityId = entityId;
    }
}
