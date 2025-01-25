package com.stepaniuk.testhorizon.testcase;

import com.stepaniuk.testhorizon.event.testcase.TestCaseEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class TestCaseProducer {
    private final KafkaTemplate<String, TestCaseEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, TestCaseEvent>> send(TestCaseEvent testCaseEvent) {
        return kafkaTemplate.send("test-cases", testCaseEvent);
    }
}
