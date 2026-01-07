package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.domain.repository.QuizRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QuizPdfExportService {

    private final QuizRepository quizRepository;

    public QuizPdfExportService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public byte[] exportQuizzesPdf() {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 50;
            float y = page.getMediaBox().getHeight() - margin;
            float leading = 16;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.newLineAtOffset(margin, y);
                cs.showText("QuizApp — Quizzes export");
                cs.endText();

                y -= 2 * leading;

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.newLineAtOffset(margin, y);

                cs.showText("ID   Title   Negative   TimeLimit(s)");
                cs.newLineAtOffset(0, -leading);

                quizRepository.findAll().forEach(q -> {
                    String line =
                            q.getId() + "   " +
                                    safe(q.getTitle(), 45) + "   " +
                                    q.isNegativePointsEnabled() + "   " +
                                    (q.getTimeLimitSeconds() == null ? "" : q.getTimeLimitSeconds());

                    try {
                        cs.showText(line);
                        cs.newLineAtOffset(0, -leading);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });

                cs.endText();
            }

            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to export PDF", e);
        }
    }

    private String safe(String s, int maxLen) {
        if (s == null) return "";
        String trimmed = s.replaceAll("\\s+", " ").trim();
        return trimmed.length() <= maxLen ? trimmed : trimmed.substring(0, Math.max(0, maxLen - 3)) + "...";
    }
}
