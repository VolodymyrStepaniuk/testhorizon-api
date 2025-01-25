package com.stepaniuk.testhorizon.comment;

import com.stepaniuk.testhorizon.event.comment.CommentEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class CommentProducer {

    private final KafkaTemplate<String, CommentEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, CommentEvent>> send(CommentEvent commentEvent) {
        return kafkaTemplate.send("comments", commentEvent);
    }
}
