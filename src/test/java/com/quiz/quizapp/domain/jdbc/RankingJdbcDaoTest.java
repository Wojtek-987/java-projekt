package com.quiz.quizapp.domain.jdbc;

import com.quiz.quizapp.api.dto.RankingRowResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@Import(RankingJdbcDao.class)
class RankingJdbcDaoTest extends PostgresJdbcTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RankingJdbcDao rankingJdbcDao;

    @Test
    void topForQuiz_filtersUnfinishedAttempts_andOrdersByScoreDescThenFinishedAtAsc() {
        long quizId = insertQuiz("Ranking Quiz");

        // finished attempts
        insertAttempt(quizId, "Alice", 10, OffsetDateTime.parse("2025-01-01T10:00:00+00:00"));
        insertAttempt(quizId, "Bob", 10, OffsetDateTime.parse("2025-01-01T09:00:00+00:00")); // earlier => should come first among score=10
        insertAttempt(quizId, "Cathy", 7, OffsetDateTime.parse("2025-01-01T08:00:00+00:00"));

        // unfinished attempt should be ignored
        insertAttemptUnfinished(quizId, "Unfinished", 999);

        List<RankingRowResponse> top = rankingJdbcDao.topForQuiz(quizId, 10);

        assertThat(top).extracting(RankingRowResponse::nickname).doesNotContain("Unfinished");
        assertThat(top).extracting(RankingRowResponse::nickname).containsExactly("Bob", "Alice", "Cathy");
        assertThat(top).extracting(RankingRowResponse::score).containsExactly(10, 10, 7);
    }

    @Test
    void topForQuiz_respectsLimit() {
        long quizId = insertQuiz("Limit Quiz");

        insertAttempt(quizId, "A", 3, OffsetDateTime.parse("2025-01-01T10:00:00+00:00"));
        insertAttempt(quizId, "B", 2, OffsetDateTime.parse("2025-01-01T10:00:01+00:00"));
        insertAttempt(quizId, "C", 1, OffsetDateTime.parse("2025-01-01T10:00:02+00:00"));

        List<RankingRowResponse> top2 = rankingJdbcDao.topForQuiz(quizId, 2);

        assertThat(top2).hasSize(2);
        assertThat(top2).extracting(RankingRowResponse::nickname).containsExactly("A", "B");
    }

    // ---------------- helpers ----------------

    private long insertQuiz(String title) {
        return jdbcTemplate.queryForObject(
                """
                insert into quizzes
                    (title, description, randomise_questions, randomise_answers, time_limit_seconds, negative_points_enabled, created_at)
                values
                    (?, '', false, false, null, false, now())
                returning id
                """,
                Long.class,
                title
        );
    }

    private long insertAttempt(long quizId, String nickname, int score, OffsetDateTime finishedAt) {
        return jdbcTemplate.queryForObject(
                """
                insert into attempts
                    (quiz_id, nickname, score, started_at, finished_at)
                values
                    (?, ?, ?, now(), ?)
                returning id
                """,
                Long.class,
                quizId, nickname, score, finishedAt
        );
    }

    private long insertAttemptUnfinished(long quizId, String nickname, int score) {
        return jdbcTemplate.queryForObject(
                """
                insert into attempts
                    (quiz_id, nickname, score, started_at, finished_at)
                values
                    (?, ?, ?, now(), null)
                returning id
                """,
                Long.class,
                quizId, nickname, score
        );
    }
}
