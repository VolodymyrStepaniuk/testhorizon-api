package com.stepaniuk.testhorizon.test;

import com.stepaniuk.testhorizon.event.test.TestEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class TestProducer {

    private final KafkaTemplate<String, TestEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, TestEvent>> send(TestEvent testEvent) {
        return kafkaTemplate.send("tests", testEvent);
    }
}
