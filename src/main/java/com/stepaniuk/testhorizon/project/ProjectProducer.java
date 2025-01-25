package com.stepaniuk.testhorizon.project;

import com.stepaniuk.testhorizon.event.project.ProjectEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class ProjectProducer {

    private final KafkaTemplate<String, ProjectEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, ProjectEvent>> send(ProjectEvent projectEvent) {
        return kafkaTemplate.send("projects", projectEvent);
    }
}
