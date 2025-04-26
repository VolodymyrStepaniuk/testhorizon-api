package com.stepaniuk.testhorizon.security.config;

import com.stepaniuk.testhorizon.security.authinfo.AuthInfoHandlerMethodArgumentResolver;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**", "/docs/**", "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/users/**").hasAnyAuthority(AuthorityName.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasAnyAuthority(AuthorityName.ADMIN.name())
                        .requestMatchers(HttpMethod.POST,"/bug-reports","/tests","/test-cases").hasAnyAuthority(AuthorityName.ADMIN.name(), AuthorityName.TESTER.name())
                        .requestMatchers(HttpMethod.PATCH,"/bug-reports/**","/tests/**","/test-cases/**").hasAnyAuthority(AuthorityName.ADMIN.name(), AuthorityName.TESTER.name())
                        .requestMatchers(HttpMethod.DELETE,"/bug-reports/**","/tests/**","/test-cases/**").hasAnyAuthority(AuthorityName.ADMIN.name(), AuthorityName.TESTER.name())
                        .requestMatchers(HttpMethod.POST,"/projects").hasAnyAuthority(AuthorityName.DEVELOPER.name(),AuthorityName.ADMIN.name())
                        .requestMatchers(HttpMethod.POST,"/ratings").hasAnyAuthority(AuthorityName.ADMIN.name(), AuthorityName.DEVELOPER.name())
                        .requestMatchers(HttpMethod.PATCH,"/projects/**").hasAnyAuthority(AuthorityName.DEVELOPER.name(),AuthorityName.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE,"/projects/**").hasAnyAuthority(AuthorityName.DEVELOPER.name(),AuthorityName.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/files/delete-folder/**").hasAnyAuthority(AuthorityName.ADMIN.name())
                        .requestMatchers(HttpMethod.GET,"/feedbacks").permitAll()
                        .requestMatchers(HttpMethod.POST,"/auth/admin/register-user").hasAuthority(AuthorityName.ADMIN.name())
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityWebMvcConfigurer securityWebMvcConfigurer() {
        return new SecurityWebMvcConfigurer();
    }

    public static class SecurityWebMvcConfigurer implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthInfoHandlerMethodArgumentResolver());
        }

    }
}
