package com.stepaniuk.testhorizon.testspecific.jwt;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@WithSecurityContext(factory = WithJwtTokenSecurityContextFactory.class)
public @interface WithJwtToken {

    long userId();

    String username() default "email@gmail.com";

    String[] authorities() default {
        "ADMIN"
    };
}
