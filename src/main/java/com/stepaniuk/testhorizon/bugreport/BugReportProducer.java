package com.stepaniuk.testhorizon.bugreport;

import com.stepaniuk.testhorizon.event.bugreport.BugReportEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class BugReportProducer {

    private final KafkaTemplate<String, BugReportEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, BugReportEvent>> send(BugReportEvent bugReportEvent) {
        return kafkaTemplate.send("bug-reports", bugReportEvent);
    }
}
