package com.quiz.quizapp.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitAnswersRequest(
        @NotEmpty List<SubmitAnswerRequest> answers
) {}
