package com.quiz.quizapp.domain.jdbc;

import com.quiz.quizapp.api.dto.RankingRowResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class RankingJdbcDaoIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.flyway.enabled", () -> "false");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    RankingJdbcDao rankingJdbcDao;

    @BeforeEach
    void schema() {
        jdbcTemplate.execute("drop table if exists attempts");
        jdbcTemplate.execute("""
                create table attempts (
                    id bigserial primary key,
                    quiz_id bigint not null,
                    nickname varchar(60) not null,
                    score int not null,
                    started_at timestamptz not null,
                    finished_at timestamptz
                )
                """);
    }

    @Test
    void topForQuiz_excludesUnfinishedAttempts() {
        OffsetDateTime now = OffsetDateTime.now();

        insertAttempt(1L, "FINISHED", 10, now.minusMinutes(10), now.minusMinutes(5));
        insertAttempt(1L, "UNFINISHED", 999, now.minusMinutes(1), null);

        List<RankingRowResponse> out = rankingJdbcDao.topForQuiz(1L, 10);

        assertThat(out).extracting(RankingRowResponse::nickname).containsExactly("FINISHED");
    }

    @Test
    void topForQuiz_excludesOtherQuizAttempts() {
        OffsetDateTime now = OffsetDateTime.now();

        insertAttempt(1L, "IN_SCOPE", 10, now.minusMinutes(10), now.minusMinutes(5));
        insertAttempt(2L, "OUT_OF_SCOPE", 999, now.minusMinutes(10), now.minusMinutes(9));

        List<RankingRowResponse> out = rankingJdbcDao.topForQuiz(1L, 10);

        assertThat(out).extracting(RankingRowResponse::nickname).containsExactly("IN_SCOPE");
    }

    @Test
    void topForQuiz_ordersByScoreDesc_thenFinishedAtAsc() {
        OffsetDateTime now = OffsetDateTime.now();

        insertAttempt(1L, "A", 10, now.minusMinutes(10), now.minusMinutes(5));
        insertAttempt(1L, "B", 10, now.minusMinutes(10), now.minusMinutes(6));
        insertAttempt(1L, "C", 20, now.minusMinutes(10), now.minusMinutes(4));

        List<RankingRowResponse> out = rankingJdbcDao.topForQuiz(1L, 10);

        assertThat(out).extracting(RankingRowResponse::nickname).containsExactly("C", "B", "A");
    }

    @Test
    void topForQuiz_appliesLimit() {
        OffsetDateTime now = OffsetDateTime.now();

        insertAttempt(1L, "C", 20, now.minusMinutes(10), now.minusMinutes(4));
        insertAttempt(1L, "B", 10, now.minusMinutes(10), now.minusMinutes(6));
        insertAttempt(1L, "A", 10, now.minusMinutes(10), now.minusMinutes(5));

        List<RankingRowResponse> out = rankingJdbcDao.topForQuiz(1L, 2);

        assertThat(out).extracting(RankingRowResponse::nickname).containsExactly("C", "B");
    }

    private void insertAttempt(Long quizId, String nickname, int score, OffsetDateTime startedAt, OffsetDateTime finishedAt) {
        jdbcTemplate.update("""
                insert into attempts(quiz_id, nickname, score, started_at, finished_at)
                values (?,?,?,?,?)
                """, quizId, nickname, score, startedAt, finishedAt);
    }
}
