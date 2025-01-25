package com.stepaniuk.testhorizon.rating;

import com.stepaniuk.testhorizon.event.rating.RatingEvent;
import com.stepaniuk.testhorizon.shared.Producer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Producer
@RequiredArgsConstructor
public class RatingProducer {

    private final KafkaTemplate<String, RatingEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, RatingEvent>> send(RatingEvent ratingEvent) {
        return kafkaTemplate.send("ratings", ratingEvent);
    }
}
