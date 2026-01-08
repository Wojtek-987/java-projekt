package com.quiz.quizapp.web;

import com.quiz.quizapp.application.creator.CreatorQuizFacade;
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

    private final CreatorQuizFacade creatorQuizFacade;

    public CreatorQuizController(CreatorQuizFacade creatorQuizFacade) {
        this.creatorQuizFacade = creatorQuizFacade;
    }

    @GetMapping
    public String list(Model model) {
        var page = creatorQuizFacade.list(PageRequest.of(0, 50));

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

        creatorQuizFacade.create(form);

        ra.addFlashAttribute("flashMessage", "Quiz created.");
        return "redirect:/creator/quizzes";
    }
}
