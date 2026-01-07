package com.quiz.quizapp.domain.repository;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
abstract class PostgresDataJpaTestBase {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("quizapp_test")
                    .withUsername("quizapp")
                    .withPassword("quizapp");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        // Crucial: start here, before Spring creates DataSource / Hikari.
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        // Make failures fast and obvious, rather than a 30s Hikari wait.
        registry.add("spring.datasource.hikari.connection-timeout", () -> "5000");
        registry.add("spring.datasource.hikari.initialization-fail-timeout", () -> "1");

        registry.add("spring.flyway.enabled", () -> "true");
    }
}
