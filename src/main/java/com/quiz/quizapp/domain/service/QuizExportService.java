package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.domain.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class QuizExportService {

    private final QuizRepository quizRepository;
    private final QuizPdfExportService quizPdfExportService;

    public QuizExportService(QuizRepository quizRepository, QuizPdfExportService quizPdfExportService) {
        this.quizRepository = quizRepository;
        this.quizPdfExportService = quizPdfExportService;
    }

    public byte[] exportQuizzesCsv() {
        var sb = new StringBuilder();
        sb.append("id,title,negativePointsEnabled,timeLimitSeconds\n");

        quizRepository.findAll().forEach(q -> {
            sb.append(q.getId()).append(",");
            sb.append(escapeCsv(q.getTitle())).append(",");
            sb.append(q.isNegativePointsEnabled()).append(",");
            sb.append(q.getTimeLimitSeconds() == null ? "" : q.getTimeLimitSeconds());
            sb.append("\n");
        });

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportQuizzesPdf() {
        return quizPdfExportService.exportQuizzesPdf();
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }
}
