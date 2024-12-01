package com.stepaniuk.testhorizon.testspecific;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ImportAutoConfiguration(TestDatabaseContainerAutoConfiguration.class)
public @interface AutoConfigureTestDatabaseContainer {

}
