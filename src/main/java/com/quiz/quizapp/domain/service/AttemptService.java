package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.api.dto.AttemptResponse;
import com.quiz.quizapp.api.dto.FinishAttemptRequest;
import com.quiz.quizapp.api.dto.StartAttemptRequest;
import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.entity.AttemptEntity;
import com.quiz.quizapp.domain.repository.AttemptRepository;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttemptService {

    private final QuizRepository quizRepository;
    private final AttemptRepository attemptRepository;

    public AttemptService(QuizRepository quizRepository, AttemptRepository attemptRepository) {
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
    }

    @Transactional
    public AttemptResponse start(long quizId, StartAttemptRequest req) {
        var quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));

        AttemptEntity attempt = new AttemptEntity(quiz, req.nickname().trim());
        AttemptEntity saved = attemptRepository.save(attempt);
        return toResponse(saved);
    }

    @Transactional
    public AttemptResponse finish(long attemptId, FinishAttemptRequest req) {
        var attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + attemptId));

        attempt.setScore(req.score());
        attempt.finishNow();
        return toResponse(attempt);
    }

    private AttemptResponse toResponse(AttemptEntity a) {
        return new AttemptResponse(
                a.getId(),
                a.getQuiz().getId(),
                a.getNickname(),
                a.getScore(),
                a.getStartedAt(),
                a.getFinishedAt()
        );
    }
}
