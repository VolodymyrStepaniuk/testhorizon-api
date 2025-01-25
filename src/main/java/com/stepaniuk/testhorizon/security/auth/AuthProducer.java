package com.stepaniuk.testhorizon.security.auth;

import com.stepaniuk.testhorizon.event.auth.AuthEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class AuthProducer {

    private final KafkaTemplate<String, AuthEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, AuthEvent>> send(AuthEvent authEvent) {
        return kafkaTemplate.send("auth", authEvent);
    }
}
