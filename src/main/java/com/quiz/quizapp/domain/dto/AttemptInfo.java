package com.quiz.quizapp.domain.dto;

import java.time.OffsetDateTime;

public record AttemptInfo(
        long id,
        long quizId,
        String nickname,
        int score,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt
) {}
