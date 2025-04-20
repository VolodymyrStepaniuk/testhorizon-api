package com.stepaniuk.testhorizon.notebook;

import com.stepaniuk.testhorizon.event.notebook.NotebookEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class NotebookProducer {
    private final KafkaTemplate<String, NotebookEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, NotebookEvent>> send(NotebookEvent notebookEvent) {
        return kafkaTemplate.send("notebooks", notebookEvent);
    }
}
