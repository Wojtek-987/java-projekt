package com.quiz.quizapp.domain.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@Import(QuizJdbcDao.class)
class QuizJdbcDaoTest extends PostgresJdbcTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private QuizJdbcDao quizJdbcDao;

    @Test
    void findQuizSummaries_returnsRowsWithQuestionCounts_includingZero() {
        long quiz1 = insertQuiz("Quiz One");
        long quiz2 = insertQuiz("Quiz Two");

        insertQuestion(quiz1, "SINGLE_CHOICE", "Q1", 1, "{\"value\":\"A\"}");
        insertQuestion(quiz1, "TRUE_FALSE", "Q2", 1, "{\"value\":true}");

        List<QuizSummaryRow> rows = quizJdbcDao.findQuizSummaries();

        assertThat(rows).extracting(QuizSummaryRow::id).contains(quiz1, quiz2);

        QuizSummaryRow r1 = rows.stream().filter(r -> r.id() == quiz1).findFirst().orElseThrow();
        QuizSummaryRow r2 = rows.stream().filter(r -> r.id() == quiz2).findFirst().orElseThrow();

        assertThat(r1.title()).isEqualTo("Quiz One");
        assertThat(r1.questionCount()).isEqualTo(2);

        assertThat(r2.title()).isEqualTo("Quiz Two");
        assertThat(r2.questionCount()).isZero();
    }

    @Test
    void updateQuizTitle_updatesExactlyOneRow() {
        long quizId = insertQuiz("Old Title");

        int updated = quizJdbcDao.updateQuizTitle(quizId, "New Title");

        assertThat(updated).isEqualTo(1);

        String title = jdbcTemplate.queryForObject(
                "select title from quizzes where id = ?",
                String.class,
                quizId
        );
        assertThat(title).isEqualTo("New Title");
    }

    @Test
    void updateQuizTitle_missingId_updatesZeroRows() {
        int updated = quizJdbcDao.updateQuizTitle(999999L, "Whatever");
        assertThat(updated).isZero();
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

    private long insertQuestion(long quizId, String type, String prompt, int points, String answerKeyJson) {
        return jdbcTemplate.queryForObject(
                """
                insert into questions
                    (quiz_id, type, prompt, points, options, answer_key, created_at)
                values
                    (?, ?, ?, ?, null, ?::jsonb, now())
                returning id
                """,
                Long.class,
                quizId, type, prompt, points, answerKeyJson
        );
    }
}
