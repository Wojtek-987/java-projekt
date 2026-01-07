package com.quiz.quizapp.api.dto;

public record QuestionForPlayResponse(
        long id,
        String type,
        String prompt,
        Integer points,
        String options // JSON string
) {}
