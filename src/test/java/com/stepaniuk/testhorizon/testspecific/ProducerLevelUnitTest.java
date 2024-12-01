package com.stepaniuk.testhorizon.testspecific;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.boot.testcontainers.service.connection.ServiceConnectionAutoConfiguration;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@OverrideAutoConfiguration(enabled = false)
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureTestKafkaContainer
//@Import({KafkaTopicConfig.class, KafkaConsumerConfig.class})
@ImportAutoConfiguration({KafkaAutoConfiguration.class, ServiceConnectionAutoConfiguration.class,
    KafkaTemplateAutoConfiguration.class})
public @interface ProducerLevelUnitTest {

}
