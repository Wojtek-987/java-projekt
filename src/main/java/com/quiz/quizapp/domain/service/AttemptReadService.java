package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.entity.AttemptEntity;
import com.quiz.quizapp.domain.repository.AttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttemptReadService {

    private final AttemptRepository attemptRepository;

    public AttemptReadService(AttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public AttemptEntity getWithQuizOrThrow(long attemptId) {
        return attemptRepository.findWithQuizById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + attemptId));
    }
}
