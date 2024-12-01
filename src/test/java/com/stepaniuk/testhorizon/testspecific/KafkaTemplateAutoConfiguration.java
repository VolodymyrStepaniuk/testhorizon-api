package com.stepaniuk.testhorizon.testspecific;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
public class KafkaTemplateAutoConfiguration {

//  @Bean
//  <K, V> InitializingBean kafkaTemplateConsumerFactoryInitializer(KafkaTemplate<K, V> kafkaTemplate,
//                                                                  ConsumerFactory<K, V> consumerFactory) {
//    return () -> {
//      kafkaTemplate.setConsumerFactory(consumerFactory);
//    };
//  }
}
