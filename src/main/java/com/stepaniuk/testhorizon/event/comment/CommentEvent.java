package com.stepaniuk.testhorizon.event.comment;

import com.stepaniuk.testhorizon.event.shared.AbstractApplicationEvent;
import com.stepaniuk.testhorizon.event.shared.ApplicationEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CommentEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final Long commentId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "commentId"})
    public CommentEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                        Long commentId) {
        super(eventType, timestamp, eventId, correlationId);
        this.commentId = commentId;
    }

}
