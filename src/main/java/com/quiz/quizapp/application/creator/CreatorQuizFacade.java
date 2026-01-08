package com.quiz.quizapp.application.creator;

import com.quiz.quizapp.domain.dto.QuizCreateCommand;
import com.quiz.quizapp.domain.dto.QuizInfo;
import com.quiz.quizapp.domain.service.QuizService;
import com.quiz.quizapp.web.dto.CreateQuizForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class CreatorQuizFacade {

    private final QuizService quizService;

    public CreatorQuizFacade(QuizService quizService) {
        this.quizService = quizService;
    }

    public Page<QuizInfo> list(Pageable pageable) {
        return quizService.list(pageable);
    }

    public QuizInfo create(CreateQuizForm form) {
        return quizService.create(new QuizCreateCommand(
                form.getTitle(),
                form.getDescription(),
                form.isRandomiseQuestions(),
                form.isRandomiseAnswers(),
                form.getTimeLimitSeconds(),
                form.isNegativePointsEnabled()
        ));
    }
}
