package com.quiz.quizapp.domain.jdbc;

import com.quiz.quizapp.api.dto.RankingRowResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RankingJdbcDao {

    private final JdbcTemplate jdbcTemplate;

    public RankingJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RankingRowResponse> topForQuiz(long quizId, int limit) {
        String sql = """
            select nickname, score
            from attempts
            where quiz_id = ? and finished_at is not null
            order by score desc, finished_at asc
            limit ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, n) -> new RankingRowResponse(rs.getString("nickname"), rs.getInt("score")),
                quizId,
                limit
        );
    }
}
