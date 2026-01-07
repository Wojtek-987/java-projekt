package com.quiz.quizapp.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartAttemptRequest(
        @NotBlank @Size(max = 60) String nickname
) {}
