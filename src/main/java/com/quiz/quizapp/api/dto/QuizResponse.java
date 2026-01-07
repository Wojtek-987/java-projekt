package com.quiz.quizapp.api.dto;

import java.time.OffsetDateTime;

public record QuizResponse(
        long id,
        String title,
        String description,
        boolean randomiseQuestions,
        boolean randomiseAnswers,
        Integer timeLimitSeconds,
        boolean negativePointsEnabled,
        OffsetDateTime createdAt
) {}
