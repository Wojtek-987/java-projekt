package com.quiz.quizapp.domain.jdbc;

public record QuizSummaryRow(
        long id,
        String title,
        int questionCount
) {}
