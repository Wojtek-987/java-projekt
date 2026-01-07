package com.quiz.quizapp.domain.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class QuizJdbcDao {

    private static final RowMapper<QuizSummaryRow> QUIZ_SUMMARY_MAPPER =
            (rs, rowNum) -> new QuizSummaryRow(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getInt("question_count")
            );

    private final JdbcTemplate jdbcTemplate;

    public QuizJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<QuizSummaryRow> findQuizSummaries() {
        String sql = """
            select q.id,
                   q.title,
                   count(que.id) as question_count
            from quizzes q
            left join questions que on que.quiz_id = q.id
            group by q.id, q.title
            order by q.id
            """;

        return jdbcTemplate.query(sql, QUIZ_SUMMARY_MAPPER);
    }

    public int updateQuizTitle(long quizId, String newTitle) {
        String sql = "update quizzes set title = ? where id = ?";
        return jdbcTemplate.update(sql, newTitle, quizId);
    }
}
