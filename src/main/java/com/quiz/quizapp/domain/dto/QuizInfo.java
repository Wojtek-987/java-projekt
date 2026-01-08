package com.quiz.quizapp.domain.dto;

import java.time.OffsetDateTime;

public record QuizInfo(
        long id,
        String title,
        String description,
        boolean randomiseQuestions,
        boolean randomiseAnswers,
        Integer timeLimitSeconds,
        boolean negativePointsEnabled,
        OffsetDateTime createdAt
) {}
