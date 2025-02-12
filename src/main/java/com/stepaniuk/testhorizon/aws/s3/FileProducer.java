package com.stepaniuk.testhorizon.aws.s3;

import com.stepaniuk.testhorizon.event.file.FileEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class FileProducer {

    private final KafkaTemplate<String, FileEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, FileEvent>> send(FileEvent fileEvent) {
        return kafkaTemplate.send("files", fileEvent);
    }
}
