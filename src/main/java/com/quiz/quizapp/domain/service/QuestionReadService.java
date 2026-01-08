package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.domain.entity.QuestionEntity;
import com.quiz.quizapp.domain.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionReadService {

    private final QuestionRepository questionRepository;

    public QuestionReadService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Transactional(readOnly = true)
    public Page<QuestionEntity> listForQuiz(long quizId, Pageable pageable) {
        return questionRepository.findByQuiz_Id(quizId, pageable);
    }
}
