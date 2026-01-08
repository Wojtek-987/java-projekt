package com.quiz.quizapp.domain.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitAnswersCommand(
        @NotEmpty List<SubmitAnswerDto> answers
) {}
