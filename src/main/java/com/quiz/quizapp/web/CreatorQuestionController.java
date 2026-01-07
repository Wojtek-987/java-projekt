package com.quiz.quizapp.web;

import com.quiz.quizapp.domain.repository.QuestionRepository;
import com.quiz.quizapp.domain.repository.QuizRepository;
import com.quiz.quizapp.domain.service.CreatorQuestionService;
import com.quiz.quizapp.web.dto.CreateQuestionForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/creator/quizzes/{quizId}/questions")
public class CreatorQuestionController {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final CreatorQuestionService creatorQuestionService;

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

    public CreatorQuestionController(
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            CreatorQuestionService creatorQuestionService
    ) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.creatorQuestionService = creatorQuestionService;
    }

    @GetMapping
    public String list(@PathVariable long quizId, Model model) {
        var quiz = quizRepository.findById(quizId).orElseThrow();
        var page = questionRepository.findByQuiz_Id(quizId, PageRequest.of(0, 200));

        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", page.getContent());
        model.addAttribute("title", "Creator • Questions");

        model.addAttribute("contentTemplate", "creator/questions");
        model.addAttribute("contentFragment", "content");

        return "fragments/layout";
    }

    @GetMapping("/new")
    public String newForm(@PathVariable long quizId, Model model) {
        var quiz = quizRepository.findById(quizId).orElseThrow();

        var form = new CreateQuestionForm();
        form.setQuizId(quizId);
        form.setPoints(1);
        form.setType("SINGLE_CHOICE");
        form.setAnswerKeyJson("{\"value\":\"\"}");

        model.addAttribute("quiz", quiz);
        model.addAttribute("types", TYPES);
        model.addAttribute("form", form);
        model.addAttribute("title", "Creator • New question");

        model.addAttribute("contentTemplate", "creator/question-new");
        model.addAttribute("contentFragment", "content");

        return "fragments/layout";
    }

    @PostMapping
    public String create(
            @PathVariable long quizId,
            @Valid @ModelAttribute("form") CreateQuestionForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes ra
    ) {
        var quiz = quizRepository.findById(quizId).orElseThrow();

        if (binding.hasErrors()) {
            model.addAttribute("quiz", quiz);
            model.addAttribute("types", TYPES);
            model.addAttribute("title", "Creator • New question");

            model.addAttribute("contentTemplate", "creator/question-new");
            model.addAttribute("contentFragment", "content");

            return "fragments/layout";
        }

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
            binding.addError(new FieldError("form", "answerKeyJson", "Invalid JSON in answerKeyJson/optionsJson"));

            model.addAttribute("quiz", quiz);
            model.addAttribute("types", TYPES);
            model.addAttribute("title", "Creator • New question");

            model.addAttribute("contentTemplate", "creator/question-new");
            model.addAttribute("contentFragment", "content");

            return "fragments/layout";
        }

        ra.addFlashAttribute("flashMessage", "Question created.");
        return "redirect:/creator/quizzes/" + quizId + "/questions";
    }
}
