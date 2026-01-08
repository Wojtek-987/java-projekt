package com.quiz.quizapp.domain.service;

import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.repository.QuizRepository;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class QuizExportServiceTest {

    @Test
    void exportQuizzesCsv_includesHeaderRow() {
        QuizRepository quizRepository = mock(QuizRepository.class);
        QuizPdfExportService pdf = mock(QuizPdfExportService.class);

        when(quizRepository.findAll()).thenReturn(List.of(new QuizEntity("T", "d")));

        QuizExportService svc = new QuizExportService(quizRepository, pdf);
        String csv = new String(svc.exportQuizzesCsv(), StandardCharsets.UTF_8);

        assertThat(csv).contains("id,title,negativePointsEnabled,timeLimitSeconds");
    }

    @Test
    void exportQuizzesCsv_escapesTitleValues() {
        QuizRepository quizRepository = mock(QuizRepository.class);
        QuizPdfExportService pdf = mock(QuizPdfExportService.class);

        var q1 = new QuizEntity("Hello, \"World\"", "d");
        when(quizRepository.findAll()).thenReturn(List.of(q1));

        QuizExportService svc = new QuizExportService(quizRepository, pdf);
        String csv = new String(svc.exportQuizzesCsv(), StandardCharsets.UTF_8);

        assertThat(csv).contains("\"Hello, \"\"World\"\"\"");
    }

    @Test
    void exportQuizzesCsv_writesBooleanAndTimeLimitFields() {
        QuizRepository quizRepository = mock(QuizRepository.class);
        QuizPdfExportService pdf = mock(QuizPdfExportService.class);

        var q1 = new QuizEntity("T", "d");
        q1.setNegativePointsEnabled(true);
        q1.setTimeLimitSeconds(60);

        when(quizRepository.findAll()).thenReturn(List.of(q1));

        QuizExportService svc = new QuizExportService(quizRepository, pdf);
        String csv = new String(svc.exportQuizzesCsv(), StandardCharsets.UTF_8);

        assertThat(csv).contains(",true,60");
    }

    @Test
    void exportQuizzesPdf_delegatesToPdfService() {
        QuizRepository quizRepository = mock(QuizRepository.class);
        QuizPdfExportService pdf = mock(QuizPdfExportService.class);

        when(pdf.exportQuizzesPdf()).thenReturn(new byte[]{1, 2, 3});

        QuizExportService svc = new QuizExportService(quizRepository, pdf);

        assertThat(svc.exportQuizzesPdf()).containsExactly(1, 2, 3);
        verify(pdf).exportQuizzesPdf();
        verifyNoMoreInteractions(pdf, quizRepository);
    }
}
