package com.quiz.quizapp.api;

import com.quiz.quizapp.domain.jdbc.QuizJdbcDao;
import com.quiz.quizapp.domain.jdbc.QuizSummaryRow;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JdbcDemoController {

    private final QuizJdbcDao quizJdbcDao;

    public JdbcDemoController(QuizJdbcDao quizJdbcDao) {
        this.quizJdbcDao = quizJdbcDao;
    }

    @GetMapping("/api/v1/jdbc/quizzes")
    public List<QuizSummaryRow> quizzes() {
        return quizJdbcDao.findQuizSummaries();
    }
}
