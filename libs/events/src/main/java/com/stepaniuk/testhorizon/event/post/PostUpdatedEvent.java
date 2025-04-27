package com.stepaniuk.testhorizon.event.post;

import com.stepaniuk.testhorizon.types.post.PostCategoryName;
import jakarta.annotation.Nullable;
import lombok.*;

import java.beans.ConstructorProperties;
import java.time.Instant;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PostUpdatedEvent extends PostEvent {

    private final Data data;

    @ConstructorProperties({"timestamp", "eventId", "correlationId", "postId", "data"})
    public PostUpdatedEvent(Instant timestamp, String eventId, String correlationId,
                            Long postId, Data data) {
        super("PostUpdatedEvent", timestamp, eventId, correlationId, postId);
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
        private String title;

        @Nullable
        private String description;

        @Nullable
        private String content;

        @Nullable
        private PostCategoryName category;

    }
}
