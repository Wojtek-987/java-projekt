package com.quiz.quizapp.application.creator;

import com.quiz.quizapp.domain.service.CreatorQuestionService;
import com.quiz.quizapp.domain.service.QuestionReadService;
import com.quiz.quizapp.domain.service.QuizReadService;
import com.quiz.quizapp.web.dto.CreateQuestionForm;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CreatorQuestionFacade {

    private static final List<String> TYPES = List.of(
            "SINGLE_CHOICE",
            "MULTI_CHOICE",
            "TRUE_FALSE",
            "SHORT_ANSWER",
            "LIST_CHOICE",
            "FILL_BLANKS",
            "SORTING",
            "MATCHING"
    );

    private final QuizReadService quizReadService;
    private final QuestionReadService questionReadService;
    private final CreatorQuestionService creatorQuestionService;

    public CreatorQuestionFacade(
            QuizReadService quizReadService,
            QuestionReadService questionReadService,
            CreatorQuestionService creatorQuestionService
    ) {
        this.quizReadService = quizReadService;
        this.questionReadService = questionReadService;
        this.creatorQuestionService = creatorQuestionService;
    }

    public Map<String, Object> getListViewModel(long quizId) {
        var quiz = quizReadService.getOrThrow(quizId);
        var page = questionReadService.listForQuiz(quizId, PageRequest.of(0, 200));

        Map<String, Object> model = new HashMap<>();
        model.put("quiz", quiz);
        model.put("questions", page.getContent());
        model.put("title", "Creator • Questions");
        model.put("contentTemplate", "creator/questions");
        model.put("contentFragment", "content");
        return model;
    }

    public Map<String, Object> getNewFormViewModel(long quizId) {
        return getNewFormViewModel(quizId, defaultFormForQuiz(quizId));
    }

    public Map<String, Object> getNewFormViewModel(long quizId, CreateQuestionForm form) {
        var quiz = quizReadService.getOrThrow(quizId);

        Map<String, Object> model = new HashMap<>();
        model.put("quiz", quiz);
        model.put("types", TYPES);
        model.put("form", form);
        model.put("title", "Creator • New question");
        model.put("contentTemplate", "creator/question-new");
        model.put("contentFragment", "content");
        return model;
    }

    public void create(long quizId, CreateQuestionForm form) {
        try {
            creatorQuestionService.create(
                    quizId,
                    form.getType(),
                    form.getPrompt(),
                    form.getPoints(),
                    form.getOptionsJson(),
                    form.getAnswerKeyJson()
            );
        } catch (IllegalArgumentException ex) {
            throw new InvalidQuestionJsonException("Invalid JSON in answerKeyJson/optionsJson", ex);
        }
    }

    private CreateQuestionForm defaultFormForQuiz(long quizId) {
        var form = new CreateQuestionForm();
        form.setQuizId(quizId);
        form.setPoints(1);
        form.setType("SINGLE_CHOICE");
        form.setAnswerKeyJson("{\"value\":\"\"}");
        return form;
    }

    public static final class InvalidQuestionJsonException extends RuntimeException {
        public InvalidQuestionJsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
