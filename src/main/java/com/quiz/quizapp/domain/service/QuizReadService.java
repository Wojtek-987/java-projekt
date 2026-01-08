package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizReadService {

    private final QuizRepository quizRepository;

    public QuizReadService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Transactional(readOnly = true)
    public Page<QuizEntity> list(Pageable pageable) {
        return quizRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public QuizEntity getOrThrow(long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));
    }
}
