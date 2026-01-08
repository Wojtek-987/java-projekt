package com.quiz.quizapp.domain.dto;

public record SubmitOutcome(
        long attemptId,
        int totalScore
) {}
