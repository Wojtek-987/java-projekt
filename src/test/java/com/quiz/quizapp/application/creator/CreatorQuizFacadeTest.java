package com.quiz.quizapp.application.creator;

import com.quiz.quizapp.domain.dto.QuizCreateCommand;
import com.quiz.quizapp.domain.dto.QuizInfo;
import com.quiz.quizapp.domain.service.QuizService;
import com.quiz.quizapp.web.dto.CreateQuizForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreatorQuizFacadeTest {

    private QuizService quizService;
    private CreatorQuizFacade facade;

    @BeforeEach
    void setUp() {
        quizService = mock(QuizService.class);
        facade = new CreatorQuizFacade(quizService);
    }

    @Test
    void list_delegatesToQuizService() {
        when(quizService.list(any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        facade.list(PageRequest.of(0, 10));

        verify(quizService).list(PageRequest.of(0, 10));
    }

    @Test
    void create_mapsFormIntoCommand() {
        CreateQuizForm form = new CreateQuizForm();
        form.setTitle("T");
        form.setDescription("D");
        form.setRandomiseQuestions(true);
        form.setRandomiseAnswers(false);
        form.setTimeLimitSeconds(30);
        form.setNegativePointsEnabled(true);

        QuizInfo fake = mock(QuizInfo.class);
        when(quizService.create(any(QuizCreateCommand.class))).thenReturn(fake);

        QuizInfo out = facade.create(form);

        assertThat(out).isSameAs(fake);

        ArgumentCaptor<QuizCreateCommand> captor = ArgumentCaptor.forClass(QuizCreateCommand.class);
        verify(quizService).create(captor.capture());

        QuizCreateCommand cmd = captor.getValue();
        assertThat(cmd.title()).isEqualTo("T");
        assertThat(cmd.description()).isEqualTo("D");
        assertThat(cmd.randomiseQuestions()).isTrue();
        assertThat(cmd.randomiseAnswers()).isFalse();
        assertThat(cmd.timeLimitSeconds()).isEqualTo(30);
        assertThat(cmd.negativePointsEnabled()).isTrue();
    }
}
