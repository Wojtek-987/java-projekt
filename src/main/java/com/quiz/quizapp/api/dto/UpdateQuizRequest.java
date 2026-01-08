package com.quiz.quizapp.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateQuizRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 1000) String description,
        boolean randomiseQuestions,
        boolean randomiseAnswers,
        @Positive @Max(36000) Integer timeLimitSeconds,
        boolean negativePointsEnabled
) {}
