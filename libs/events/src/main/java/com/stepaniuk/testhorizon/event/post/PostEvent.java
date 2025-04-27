package com.stepaniuk.testhorizon.event.post;

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
public class PostEvent extends AbstractApplicationEvent implements ApplicationEvent {

    private final Long postId;

    @ConstructorProperties({"eventType", "timestamp", "eventId", "correlationId", "postId"})
    public PostEvent(String eventType, Instant timestamp, String eventId, String correlationId,
                        Long postId) {
        super(eventType, timestamp, eventId, correlationId);
        this.postId = postId;
    }
}
