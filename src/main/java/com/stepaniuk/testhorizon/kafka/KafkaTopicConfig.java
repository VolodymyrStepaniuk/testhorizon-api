package com.stepaniuk.testhorizon.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    @Bean
    public NewTopic usersTopic() {
        return TopicBuilder.name("users")
                .build();
    }

    @Bean
    public NewTopic authTopic() {
        return TopicBuilder.name("auth")
                .build();
    }

    @Bean
    public NewTopic testCaseTopic() {
        return TopicBuilder.name("test-cases")
                .build();
    }

    @Bean
    public NewTopic testTopic() {
        return TopicBuilder.name("tests")
                .build();
    }

    @Bean
    public NewTopic ratingTopic() {
        return TopicBuilder.name("ratings")
                .build();
    }

    @Bean
    public NewTopic projectTopic() {
        return TopicBuilder.name("projects")
                .build();
    }

    @Bean
    public NewTopic commentTopic() {
        return TopicBuilder.name("comments")
                .build();
    }

    @Bean
    public NewTopic bugReportTopic() {
        return TopicBuilder.name("bug-reports")
                .build();
    }

    @Bean
    public NewTopic fileTopic() {
        return TopicBuilder.name("files")
                .build();
    }

    @Bean
    public NewTopic feedbackTopic() {
        return TopicBuilder.name("feedbacks")
                .build();
    }
}
