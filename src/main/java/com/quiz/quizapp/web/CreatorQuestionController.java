package com.quiz.quizapp.web;

import com.quiz.quizapp.application.creator.CreatorQuestionFacade;
import com.quiz.quizapp.web.dto.CreateQuestionForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/creator/quizzes/{quizId}/questions")
public class CreatorQuestionController {

    private final CreatorQuestionFacade creatorQuestionFacade;

    public CreatorQuestionController(CreatorQuestionFacade creatorQuestionFacade) {
        this.creatorQuestionFacade = creatorQuestionFacade;
    }

    @GetMapping
    public String list(@PathVariable long quizId, Model model) {
        model.addAllAttributes(creatorQuestionFacade.getListViewModel(quizId));
        return "fragments/layout";
    }

    @GetMapping("/new")
    public String newForm(@PathVariable long quizId, Model model) {
        model.addAllAttributes(creatorQuestionFacade.getNewFormViewModel(quizId));
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
        if (binding.hasErrors()) {
            model.addAllAttributes(creatorQuestionFacade.getNewFormViewModel(quizId, form));
            return "fragments/layout";
        }

        try {
            creatorQuestionFacade.create(quizId, form);
        } catch (CreatorQuestionFacade.InvalidQuestionJsonException ex) {
            binding.rejectValue("answerKeyJson", "invalid.json", "Invalid JSON in answerKeyJson/optionsJson");
            model.addAllAttributes(creatorQuestionFacade.getNewFormViewModel(quizId, form));
            return "fragments/layout";
        }

        ra.addFlashAttribute("flashMessage", "Question created.");
        return "redirect:/creator/quizzes/" + quizId + "/questions";
    }
}
