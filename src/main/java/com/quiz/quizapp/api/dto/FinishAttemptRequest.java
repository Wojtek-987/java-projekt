package com.quiz.quizapp.api.dto;

import jakarta.validation.constraints.NotNull;

public record FinishAttemptRequest(
        @NotNull Integer score
) {}
