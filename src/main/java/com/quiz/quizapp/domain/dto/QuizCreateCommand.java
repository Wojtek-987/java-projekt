package com.quiz.quizapp.domain.dto;

public record QuizCreateCommand(
        String title,
        String description,
        boolean randomiseQuestions,
        boolean randomiseAnswers,
        Integer timeLimitSeconds,
        boolean negativePointsEnabled
) {}
