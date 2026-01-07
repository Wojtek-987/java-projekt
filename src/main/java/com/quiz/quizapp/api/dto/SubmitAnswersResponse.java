package com.quiz.quizapp.api.dto;

public record SubmitAnswersResponse(
        long attemptId,
        int totalScore
) {}
