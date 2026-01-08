package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class QuizPdfExportServiceTest {

    private QuizRepository quizRepository;
    private QuizPdfExportService service;

    @BeforeEach
    void setUp() {
        quizRepository = mock(QuizRepository.class);
        service = new QuizPdfExportService(quizRepository);
    }

    @Test
    void exportQuizzesPdf_returnsBytesBeginningWithPdfHeader() {
        QuizEntity q1 = new QuizEntity("A title", "D");
        setId(q1, 1L);
        q1.setNegativePointsEnabled(true);
        q1.setTimeLimitSeconds(10);

        QuizEntity q2 = new QuizEntity("   This    title    is   extremely   long   and   should   be   truncated   nicely   ", "D");
        setId(q2, 2L);
        q2.setNegativePointsEnabled(false);
        q2.setTimeLimitSeconds(null);

        when(quizRepository.findAll()).thenReturn(List.of(q1, q2));

        byte[] pdf = service.exportQuizzesPdf();

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, Math.min(pdf.length, 8))).contains("%PDF");
    }

    @Test
    void exportQuizzesPdf_producesPdfReadableByPdfBox() throws Exception {
        QuizEntity q1 = new QuizEntity("A title", "D");
        setId(q1, 1L);

        when(quizRepository.findAll()).thenReturn(List.of(q1));

        byte[] pdf = service.exportQuizzesPdf();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void exportQuizzesPdf_handlesNullTitle_safely() {
        QuizEntity q = new QuizEntity("temp", "D");
        setId(q, 1L);

        try {
            Field title = QuizEntity.class.getDeclaredField("title");
            title.setAccessible(true);
            title.set(q, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(quizRepository.findAll()).thenReturn(List.of(q));

        byte[] pdf = service.exportQuizzesPdf();

        assertThat(pdf).isNotEmpty();
    }

    private static void setId(QuizEntity q, long id) {
        try {
            Field f = QuizEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(q, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
