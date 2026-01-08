package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.dto.AttemptInfo;
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
    public AttemptInfo start(long quizId, String nickname) {
        var quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));

        String cleanedNickname = nickname == null ? "" : nickname.trim();
        AttemptEntity attempt = new AttemptEntity(quiz, cleanedNickname);
        AttemptEntity saved = attemptRepository.save(attempt);
        return toInfo(saved);
    }

    @Transactional
    public AttemptInfo finish(long attemptId, int score) {
        var attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + attemptId));

        attempt.setScore(score);
        attempt.finishNow();
        return toInfo(attempt);
    }

    private AttemptInfo toInfo(AttemptEntity a) {
        return new AttemptInfo(
                a.getId(),
                a.getQuiz().getId(),
                a.getNickname(),
                a.getScore(),
                a.getStartedAt(),
                a.getFinishedAt()
        );
    }
}
