package com.quiz.quizapp.api.dto;

import java.time.OffsetDateTime;

public record AttemptResponse(
        long id,
        long quizId,
        String nickname,
        int score,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt
) {}
