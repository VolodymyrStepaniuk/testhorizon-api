package com.stepaniuk.testhorizon.testspecific.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationProvider;

@Configuration
public class TestAppConfiguration {

    @Bean
    AuthenticationProvider authenticationProvider(){
        return new TestingAuthenticationProvider();
    }

}
