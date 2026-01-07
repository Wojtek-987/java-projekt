package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.api.dto.CreateQuizRequest;
import com.quiz.quizapp.api.dto.QuizResponse;
import com.quiz.quizapp.common.ResourceNotFoundException;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.quiz.quizapp.api.dto.UpdateQuizRequest;


@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Transactional(readOnly = true)
    public Page<QuizResponse> list(Pageable pageable) {
        return quizRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public QuizResponse get(long id) {
        QuizEntity entity = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + id));
        return toResponse(entity);
    }

    @Transactional
    public QuizResponse create(CreateQuizRequest req) {
        QuizEntity q = new QuizEntity(req.title(), req.description());
        q.setRandomiseQuestions(req.randomiseQuestions());
        q.setRandomiseAnswers(req.randomiseAnswers());
        q.setTimeLimitSeconds(req.timeLimitSeconds());
        q.setNegativePointsEnabled(req.negativePointsEnabled());

        QuizEntity saved = quizRepository.save(q);
        return toResponse(saved);
    }

    @Transactional
    public QuizResponse update(long id, UpdateQuizRequest req) {
        QuizEntity q = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + id));

        q.setTitle(req.title());
        q.setDescription(req.description());
        q.setRandomiseQuestions(req.randomiseQuestions());
        q.setRandomiseAnswers(req.randomiseAnswers());
        q.setTimeLimitSeconds(req.timeLimitSeconds());
        q.setNegativePointsEnabled(req.negativePointsEnabled());

        QuizEntity saved = quizRepository.save(q);
        return toResponse(saved);
    }

    @Transactional
    public void delete(long id) {
        if (!quizRepository.existsById(id)) {
            throw new ResourceNotFoundException("Quiz not found: " + id);
        }
        quizRepository.deleteById(id);
    }

    private QuizResponse toResponse(QuizEntity q) {
        return new QuizResponse(
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
