package com.quiz.quizapp.domain.dto;

public record SubmitAnswerDto(
        long questionId,
        String answerJson
) {}
