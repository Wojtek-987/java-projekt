package com.quiz.quizapp.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.api.dto.StartAttemptRequest;
import com.quiz.quizapp.domain.jdbc.RankingJdbcDao;
import com.quiz.quizapp.domain.repository.AttemptRepository;
import com.quiz.quizapp.domain.repository.QuizRepository;
import com.quiz.quizapp.domain.service.AttemptService;
import com.quiz.quizapp.domain.service.GameplayService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class PlayController {

    private final QuizRepository quizRepository;
    private final AttemptService attemptService;
    private final AttemptRepository attemptRepository;
    private final GameplayService gameplayService;
    private final RankingJdbcDao rankingJdbcDao;
    private final ObjectMapper objectMapper;

    public PlayController(
            QuizRepository quizRepository,
            AttemptService attemptService,
            AttemptRepository attemptRepository,
            GameplayService gameplayService,
            RankingJdbcDao rankingJdbcDao,
            ObjectMapper objectMapper
    ) {
        this.quizRepository = quizRepository;
        this.attemptService = attemptService;
        this.attemptRepository = attemptRepository;
        this.gameplayService = gameplayService;
        this.rankingJdbcDao = rankingJdbcDao;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/play")
    public String home(Model model) {
        var quizzes = quizRepository.findAll(PageRequest.of(0, 50)).getContent();
        model.addAttribute("quizzes", quizzes);
        return "play/home";
    }

    @GetMapping("/play/quizzes/{quizId}")
    public String startForm(@PathVariable long quizId, Model model) {
        var quiz = quizRepository.findById(quizId).orElseThrow();
        model.addAttribute("quiz", quiz);
        return "play/start";
    }

    @PostMapping("/play/quizzes/{quizId}/start")
    public String start(
            @PathVariable long quizId,
            @RequestParam @NotBlank String nickname
    ) {
        var attempt = attemptService.start(quizId, new StartAttemptRequest(nickname));
        return "redirect:/play/attempts/" + attempt.id();
    }

    @GetMapping("/play/attempts/{attemptId}")
    public String attempt(@PathVariable long attemptId, Model model) {
        var attempt = attemptRepository.findWithQuizById(attemptId).orElseThrow();
        var quiz = attempt.getQuiz();
        boolean randomiseAnswers = quiz.isRandomiseAnswers();

        var questions = gameplayService.questionsForAttempt(attemptId);

        // Parse options JSON arrays into lists for Thymeleaf consumption
        var optionsByQuestionId = new HashMap<Long, List<String>>();
        for (var q : questions) {
            if (q.options() == null || q.options().isBlank()) continue;

            try {
                var node = objectMapper.readTree(q.options());
                if (node.isArray()) {
                    var list = new ArrayList<String>();
                    node.forEach(n -> list.add(n.asText()));

                    if (randomiseAnswers && list.size() > 1) {
                        Collections.shuffle(list);
                    }

                    optionsByQuestionId.put(q.id(), list);
                }
            } catch (Exception ignored) {
                // ignore malformed options
            }
        }


        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("optionsByQuestionId", optionsByQuestionId);

        // Your template currently expects these names:
        model.addAttribute("quizTitle", quiz.getTitle());
        model.addAttribute("timeLimitSeconds", quiz.getTimeLimitSeconds());
        model.addAttribute("negativePointsEnabled", quiz.isNegativePointsEnabled());

        return "play/attempt";
    }

    @PostMapping("/play/attempts/{attemptId}/submit")
    public String submit(
            @PathVariable long attemptId,
            @RequestParam MultiValueMap<String, String> params,
            RedirectAttributes ra
    ) {
        var attempt = attemptRepository.findWithQuizById(attemptId).orElseThrow();
        var quizId = attempt.getQuiz().getId();

        var questions = gameplayService.questionsForAttempt(attemptId);
        var answers = new ArrayList<com.quiz.quizapp.api.dto.SubmitAnswerRequest>();

        for (var q : questions) {
            String answerJson = buildAnswerJsonFromForm(q, params);
            if (answerJson == null || answerJson.isBlank()) {
                ra.addFlashAttribute("flashMessage", "You must answer every question.");
                return "redirect:/play/attempts/" + attemptId;
            }
            answers.add(new com.quiz.quizapp.api.dto.SubmitAnswerRequest(q.id(), answerJson));
        }

        try {
            var result = gameplayService.submitAndFinish(
                    attemptId,
                    new com.quiz.quizapp.api.dto.SubmitAnswersRequest(answers)
            );
            ra.addFlashAttribute("lastScore", result.totalScore());
            return "redirect:/play/quizzes/" + quizId + "/ranking";
        } catch (IllegalStateException ex) {
            // e.g. "Time limit exceeded" or "Attempt already finished"
            ra.addFlashAttribute("flashMessage", ex.getMessage());
            return "redirect:/play/attempts/" + attemptId;
        }
    }

    @GetMapping("/play/quizzes/{quizId}/ranking")
    public String ranking(@PathVariable long quizId, Model model, @RequestParam(defaultValue = "10") int limit) {
        var quiz = quizRepository.findById(quizId).orElseThrow();
        var rows = rankingJdbcDao.topForQuiz(quizId, limit);

        model.addAttribute("quiz", quiz);
        model.addAttribute("rows", rows);
        return "play/ranking";
    }

    private String buildAnswerJsonFromForm(com.quiz.quizapp.api.dto.QuestionForPlayResponse q,
                                           MultiValueMap<String, String> params) {
        String type = q.type();

        try {
            return switch (type) {
                case "SINGLE_CHOICE", "LIST_CHOICE" -> {
                    String v = params.getFirst("q_" + q.id());
                    if (v == null || v.isBlank()) yield null;
                    yield objectMapper.writeValueAsString(Map.of("value", v));
                }
                case "TRUE_FALSE" -> {
                    String v = params.getFirst("q_" + q.id());
                    if (v == null || v.isBlank()) yield null;
                    boolean b = Boolean.parseBoolean(v);
                    yield objectMapper.writeValueAsString(Map.of("value", b));
                }
                case "MULTI_CHOICE" -> {
                    List<String> vs = params.get("q_" + q.id());
                    if (vs == null || vs.isEmpty()) yield null;
                    var cleaned = vs.stream().filter(s -> s != null && !s.isBlank()).toList();
                    if (cleaned.isEmpty()) yield null;
                    yield objectMapper.writeValueAsString(Map.of("values", cleaned));
                }
                case "SHORT_ANSWER" -> {
                    String v = params.getFirst("q_" + q.id());
                    if (v == null || v.isBlank()) yield null;
                    yield objectMapper.writeValueAsString(Map.of("value", v));
                }
                case "FILL_BLANKS" -> {
                    var values = new ArrayList<String>();
                    for (int i = 0; ; i++) {
                        String v = params.getFirst("q_" + q.id() + "_" + i);
                        if (v == null) break; // stop when field absent
                        if (v.isBlank()) yield null;
                        values.add(v);
                    }
                    if (values.isEmpty()) yield null;
                    yield objectMapper.writeValueAsString(Map.of("values", values));
                }
                case "SORTING" -> {
                    String csv = params.getFirst("q_" + q.id() + "_csv");
                    if (csv == null || csv.isBlank()) yield null;
                    var values = Arrays.stream(csv.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .toList();
                    if (values.isEmpty()) yield null;
                    yield objectMapper.writeValueAsString(Map.of("values", values));
                }
                case "MATCHING" -> {
                    String raw = params.getFirst("q_" + q.id() + "_pairs");
                    if (raw == null || raw.isBlank()) yield null;

                    Map<String, String> pairs = new LinkedHashMap<>();
                    for (String line : raw.split("\\R")) {
                        String trimmed = line.trim();
                        if (trimmed.isBlank()) continue;

                        int idx = trimmed.indexOf('=');
                        if (idx <= 0 || idx == trimmed.length() - 1) yield null;

                        String left = trimmed.substring(0, idx).trim();
                        String right = trimmed.substring(idx + 1).trim();
                        if (left.isBlank() || right.isBlank()) yield null;

                        pairs.put(left, right);
                    }
                    if (pairs.isEmpty()) yield null;

                    yield objectMapper.writeValueAsString(Map.of("pairs", pairs));
                }
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }
}
