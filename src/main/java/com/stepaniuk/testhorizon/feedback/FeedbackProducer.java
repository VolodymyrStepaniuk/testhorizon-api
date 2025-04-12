package com.stepaniuk.testhorizon.feedback;

import com.stepaniuk.testhorizon.event.feedback.FeedbackEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class FeedbackProducer {

    private final KafkaTemplate<String, FeedbackEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, FeedbackEvent>> send(FeedbackEvent feedbackEvent) {
        return kafkaTemplate.send("feedbacks", feedbackEvent);
    }
}
