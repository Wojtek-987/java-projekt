package com.quiz.quizapp.web;

import com.quiz.quizapp.application.creator.CreatorQuestionFacade;
import com.quiz.quizapp.application.play.PlayFacade;
import com.quiz.quizapp.common.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(annotations = Controller.class)
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String notFound(ResourceNotFoundException ex, HttpServletRequest req, RedirectAttributes ra) {
        ra.addFlashAttribute("flashMessage", safe(ex.getMessage(), "Not found."));
        return redirectBack(req);
    }

    @ExceptionHandler(PlayFacade.IncompleteAnswersException.class)
    public String incompleteAnswers(PlayFacade.IncompleteAnswersException ex, HttpServletRequest req, RedirectAttributes ra) {
        ra.addFlashAttribute("flashMessage", safe(ex.getMessage(), "You must answer every question."));
        return redirectBack(req);
    }

    @ExceptionHandler(CreatorQuestionFacade.InvalidQuestionJsonException.class)
    public String invalidJson(CreatorQuestionFacade.InvalidQuestionJsonException ex, HttpServletRequest req, RedirectAttributes ra) {
        ra.addFlashAttribute("flashMessage", safe(ex.getMessage(), "Invalid JSON."));
        return redirectBack(req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String badRequest(IllegalArgumentException ex, HttpServletRequest req, RedirectAttributes ra) {
        ra.addFlashAttribute("flashMessage", safe(ex.getMessage(), "Invalid request."));
        return redirectBack(req);
    }

    @ExceptionHandler(IllegalStateException.class)
    public String conflict(IllegalStateException ex, HttpServletRequest req, RedirectAttributes ra) {
        ra.addFlashAttribute("flashMessage", safe(ex.getMessage(), "Operation not allowed."));
        return redirectBack(req);
    }

    private String redirectBack(HttpServletRequest req) {
        String referer = req.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }

    private String safe(String msg, String fallback) {
        if (msg == null || msg.isBlank()) return fallback;
        return msg.length() > 300 ? msg.substring(0, 297) + "..." : msg;
    }
}
