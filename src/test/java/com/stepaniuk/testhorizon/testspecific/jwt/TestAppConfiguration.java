package com.stepaniuk.testhorizon.testspecific.jwt;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationProvider;

@TestConfiguration
public class TestAppConfiguration {

    @Bean
    AuthenticationProvider authenticationProvider(){
        return new TestingAuthenticationProvider();
    }

}
