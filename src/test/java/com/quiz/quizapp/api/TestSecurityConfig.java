package com.quiz.quizapp.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test-only security: keep it deterministic for controller tests.
 *
 * Rules:
 * - GET endpoints: permitted
 * - POST/PUT/DELETE endpoints: require authentication
 * - CSRF disabled (so we can test without form CSRF concerns)
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/**").authenticated()
                        .anyRequest().denyAll()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
