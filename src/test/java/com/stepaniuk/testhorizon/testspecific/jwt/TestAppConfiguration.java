package com.stepaniuk.testhorizon.testspecific.jwt;

import com.stepaniuk.testhorizon.security.JwtProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationProvider;

@TestConfiguration
public class TestAppConfiguration {

    @Bean
    public JwtProvider jwtProvider() {
        return new JwtProvider();
    }

    @Bean
    AuthenticationProvider authenticationProvider(){
        return new TestingAuthenticationProvider();
    }

}
