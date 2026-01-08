package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.dto.QuizCreateCommand;
import com.quiz.quizapp.domain.dto.QuizInfo;
import com.quiz.quizapp.domain.dto.QuizUpdateCommand;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Transactional(readOnly = true)
    public Page<QuizInfo> list(Pageable pageable) {
        return quizRepository.findAll(pageable).map(this::toInfo);
    }

    @Transactional(readOnly = true)
    public QuizInfo get(long id) {
        QuizEntity entity = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + id));
        return toInfo(entity);
    }

    @Transactional
    public QuizInfo create(QuizCreateCommand cmd) {
        QuizEntity q = new QuizEntity(cmd.title(), cmd.description());
        q.setRandomiseQuestions(cmd.randomiseQuestions());
        q.setRandomiseAnswers(cmd.randomiseAnswers());
        q.setTimeLimitSeconds(cmd.timeLimitSeconds());
        q.setNegativePointsEnabled(cmd.negativePointsEnabled());

        QuizEntity saved = quizRepository.save(q);
        return toInfo(saved);
    }

    @Transactional
    public QuizInfo update(long id, QuizUpdateCommand cmd) {
        QuizEntity q = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + id));

        q.setTitle(cmd.title());
        q.setDescription(cmd.description());
        q.setRandomiseQuestions(cmd.randomiseQuestions());
        q.setRandomiseAnswers(cmd.randomiseAnswers());
        q.setTimeLimitSeconds(cmd.timeLimitSeconds());
        q.setNegativePointsEnabled(cmd.negativePointsEnabled());

        QuizEntity saved = quizRepository.save(q);
        return toInfo(saved);
    }

    @Transactional
    public void delete(long id) {
        if (!quizRepository.existsById(id)) {
            throw new ResourceNotFoundException("Quiz not found: " + id);
        }
        quizRepository.deleteById(id);
    }

    private QuizInfo toInfo(QuizEntity q) {
        return new QuizInfo(
                q.getId(),
                q.getTitle(),
                q.getDescription(),
                q.isRandomiseQuestions(),
                q.isRandomiseAnswers(),
                q.getTimeLimitSeconds(),
                q.isNegativePointsEnabled(),
                q.getCreatedAt()
        );
    }
}
