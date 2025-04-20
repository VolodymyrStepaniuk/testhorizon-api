package com.stepaniuk.testhorizon.notebook.note;

import com.stepaniuk.testhorizon.event.notebook.note.NoteEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class NoteProducer {
    private final KafkaTemplate<String, NoteEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, NoteEvent>> send(NoteEvent noteEvent) {
        return kafkaTemplate.send("notes", noteEvent);
    }
}
