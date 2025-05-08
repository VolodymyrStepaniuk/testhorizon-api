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
    private static final String[] PUBLIC_MATCHERS = {
            "/auth/**",
            "/docs/**",
            "/actuator/**",
            "/feedbacks",
            "/posts",
            "/posts/**",
            "/comments"
    };

    private static final String[] ADMIN_ONLY_MATCHERS = {
            "/users/**",
            "/files/delete-folder/**",
            "/auth/admin/register-user"
    };

    private static final String[] PROJECT_WRITE_MATCHERS = {
            "/projects"
    };
    private static final String[] PROJECT_MODIFY_MATCHERS = {
            "/projects/**"
    };

    private static final String[] TESTER_CREATE_MATCHERS = {
            "/bug-reports",
            "/tests",
            "/test-cases"
    };
    private static final String[] TESTER_MODIFY_MATCHERS = {
            "/bug-reports/**",
            "/tests/**",
            "/test-cases/**"
    };

    private static final String[] RATING_MATCHERS = {
            "/ratings"
    };

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()

                        // Admin only
                        .requestMatchers(HttpMethod.PATCH, ADMIN_ONLY_MATCHERS).hasAuthority(AuthorityName.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, ADMIN_ONLY_MATCHERS).hasAuthority(AuthorityName.ADMIN.name())

                        // Tester & Admin can create and modify bug-reports, tests, test-cases
                        .requestMatchers(HttpMethod.POST, TESTER_CREATE_MATCHERS)
                        .hasAnyAuthority(AuthorityName.ADMIN.name(), AuthorityName.TESTER.name())
                        .requestMatchers(HttpMethod.PATCH, TESTER_MODIFY_MATCHERS)
                        .hasAnyAuthority(AuthorityName.ADMIN.name(), AuthorityName.TESTER.name())
                        .requestMatchers(HttpMethod.DELETE, TESTER_MODIFY_MATCHERS)
                        .hasAnyAuthority(AuthorityName.ADMIN.name(), AuthorityName.TESTER.name())

                        // Developer & Admin on projects
                        .requestMatchers(HttpMethod.POST, PROJECT_WRITE_MATCHERS)
                        .hasAnyAuthority(AuthorityName.MENTOR.name(), AuthorityName.ADMIN.name())
                        .requestMatchers(HttpMethod.PATCH, PROJECT_MODIFY_MATCHERS)
                        .hasAnyAuthority(AuthorityName.MENTOR.name(), AuthorityName.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, PROJECT_MODIFY_MATCHERS)
                        .hasAnyAuthority(AuthorityName.MENTOR.name(), AuthorityName.ADMIN.name())

                        // Ratings: Developer & Admin
                        .requestMatchers(HttpMethod.POST, RATING_MATCHERS)
                        .hasAnyAuthority(AuthorityName.MENTOR.name(), AuthorityName.ADMIN.name())

                        // everything else needs authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
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
