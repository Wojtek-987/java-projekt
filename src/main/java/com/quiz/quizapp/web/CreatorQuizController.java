package com.quiz.quizapp.web;

import com.quiz.quizapp.api.dto.CreateQuizRequest;
import com.quiz.quizapp.domain.service.QuizService;
import com.quiz.quizapp.web.dto.CreateQuizForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/creator/quizzes")
public class CreatorQuizController {

    private final QuizService quizService;

    public CreatorQuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public String list(Model model) {
        var page = quizService.list(PageRequest.of(0, 50));

        model.addAttribute("quizzes", page.getContent());
        model.addAttribute("title", "Creator • Quizzes");

        model.addAttribute("contentTemplate", "creator/quizzes");
        model.addAttribute("contentFragment", "content");

        return "fragments/layout";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new CreateQuizForm());
        model.addAttribute("title", "Creator • New quiz");

        model.addAttribute("contentTemplate", "creator/quiz-new");
        model.addAttribute("contentFragment", "content");

        return "fragments/layout";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") CreateQuizForm form,
            BindingResult binding,
            Model model,
            RedirectAttributes ra
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("title", "Creator • New quiz");

            model.addAttribute("contentTemplate", "creator/quiz-new");
            model.addAttribute("contentFragment", "content");

            return "fragments/layout";
        }

        quizService.create(new CreateQuizRequest(
                form.getTitle(),
                form.getDescription(),
                form.isRandomiseQuestions(),
                form.isRandomiseAnswers(),
                form.getTimeLimitSeconds(),
                form.isNegativePointsEnabled()
        ));

        ra.addFlashAttribute("flashMessage", "Quiz created.");
        return "redirect:/creator/quizzes";
    }
}
