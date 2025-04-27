package com.stepaniuk.testhorizon.post;


import com.stepaniuk.testhorizon.event.post.PostEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class PostProducer {
    private final KafkaTemplate<String, PostEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, PostEvent>> send(PostEvent postEvent) {
        return kafkaTemplate.send("posts", postEvent);
    }
}
