package com.quiz.quizapp.web;

import com.quiz.quizapp.application.play.PlayFacade;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/play")
public class PlayController {

    private final PlayFacade playFacade;

    public PlayController(PlayFacade playFacade) {
        this.playFacade = playFacade;
    }

    @GetMapping
    public String home(Model model) {
        model.addAttribute("quizzes", playFacade.listPlayableQuizzes());
        return "play/home";
    }

    @GetMapping("/quizzes/{quizId}")
    public String startForm(@PathVariable long quizId, Model model) {
        model.addAllAttributes(playFacade.getStartViewModel(quizId));
        return "play/start";
    }

    @PostMapping("/quizzes/{quizId}/start")
    public String start(@PathVariable long quizId, @RequestParam @NotBlank String nickname) {
        long attemptId = playFacade.startAttempt(quizId, nickname);
        return "redirect:/play/attempts/" + attemptId;
    }

    @GetMapping("/attempts/{attemptId}")
    public String attempt(@PathVariable long attemptId, Model model) {
        model.addAllAttributes(playFacade.getAttemptViewModel(attemptId));
        return "play/attempt";
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public String submit(
            @PathVariable long attemptId,
            @RequestParam MultiValueMap<String, String> params,
            RedirectAttributes ra
    ) {
        // Errors are handled globally by WebExceptionHandler (flash + redirect-back).
        PlayFacade.SubmitOutcome outcome = playFacade.submitAttempt(attemptId, params);
        ra.addFlashAttribute("lastScore", outcome.totalScore());
        return "redirect:/play/quizzes/" + outcome.quizId() + "/ranking";
    }

    @GetMapping("/quizzes/{quizId}/ranking")
    public String ranking(@PathVariable long quizId, Model model, @RequestParam(defaultValue = "10") int limit) {
        model.addAllAttributes(playFacade.getRankingViewModel(quizId, limit));
        return "play/ranking";
    }
}
