package com.quiz.quizapp.domain.dto;

public record QuestionForPlayDto(
        long id,
        String type,
        String prompt,
        Integer points,
        String optionsJson
) {}
