package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.event.user.UserEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class UserProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, UserEvent>> send(UserEvent userEvent) {
        return kafkaTemplate.send("users", userEvent);
    }

}
