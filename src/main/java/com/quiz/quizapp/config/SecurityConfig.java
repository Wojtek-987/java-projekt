package com.quiz.quizapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.http.HttpMethod.*;


@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/error",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v1/ping",
                                "/play/**"
                        ).permitAll()

                        // Public READ API
                        .requestMatchers(GET, "/api/v1/quizzes/**").permitAll()
                        .requestMatchers(POST, "/api/v1/quizzes/*/attempts").permitAll()
                        .requestMatchers(POST, "/api/v1/attempts/*/finish").permitAll()
                        .requestMatchers(GET,  "/api/v1/attempts/*/questions").permitAll()
                        .requestMatchers(POST, "/api/v1/attempts/*/submit").permitAll()
                        .requestMatchers(GET,  "/api/v1/quizzes/*/ranking").permitAll()
                        .requestMatchers(GET,  "/api/v1/jdbc/quizzes").permitAll()


                        // Restricted WRITE API
                        .requestMatchers(POST, "/api/v1/quizzes/**").hasRole("CREATOR")
                        .requestMatchers(PUT, "/api/v1/quizzes/**").hasRole("CREATOR")
                        .requestMatchers(DELETE, "/api/v1/quizzes/**").hasRole("CREATOR")

                        // Creator UI
                        .requestMatchers("/creator/**").hasRole("CREATOR")

                        // Everything else requires auth
                        .anyRequest().authenticated()
                )


                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout.permitAll())

                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> response.sendError(401),
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                )
                .build();
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
