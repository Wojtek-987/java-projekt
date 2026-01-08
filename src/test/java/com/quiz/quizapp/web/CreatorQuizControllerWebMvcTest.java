package com.quiz.quizapp.web;

import com.quiz.quizapp.application.creator.CreatorQuizFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = CreatorQuizController.class)
@AutoConfigureMockMvc(addFilters = false)
class CreatorQuizControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CreatorQuizFacade creatorQuizFacade;

    @Test
    void list_rendersLayoutViewName() throws Exception {
        when(creatorQuizFacade.list(any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 50), 0));

        mvc.perform(get("/creator/quizzes"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/layout"));

        verify(creatorQuizFacade).list(any());
        verifyNoMoreInteractions(creatorQuizFacade);
    }

    @Test
    void list_exposesExpectedModelAttributes() throws Exception {
        when(creatorQuizFacade.list(any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 50), 0));

        mvc.perform(get("/creator/quizzes"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("quizzes", "title", "contentTemplate", "contentFragment"));

        verify(creatorQuizFacade).list(any());
        verifyNoMoreInteractions(creatorQuizFacade);
    }

    @Test
    void newForm_rendersLayoutViewName() throws Exception {
        mvc.perform(get("/creator/quizzes/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/layout"));

        verifyNoMoreInteractions(creatorQuizFacade);
    }

    @Test
    void newForm_exposesExpectedModelAttributes() throws Exception {
        mvc.perform(get("/creator/quizzes/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("form", "title", "contentTemplate", "contentFragment"));

        verifyNoMoreInteractions(creatorQuizFacade);
    }

    @Test
    void create_success_redirectsToList() throws Exception {
        mvc.perform(post("/creator/quizzes")
                        .param("title", "T")
                        .param("description", "D")
                        .param("randomiseQuestions", "true")
                        .param("randomiseAnswers", "false")
                        .param("negativePointsEnabled", "true")
                        .param("timeLimitSeconds", "30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/creator/quizzes"));

        verify(creatorQuizFacade).create(any());
        verifyNoMoreInteractions(creatorQuizFacade);
    }

    @Test
    void create_success_setsFlashMessage() throws Exception {
        mvc.perform(post("/creator/quizzes")
                        .param("title", "T")
                        .param("description", "D")
                        .param("randomiseQuestions", "true")
                        .param("randomiseAnswers", "false")
                        .param("negativePointsEnabled", "true")
                        .param("timeLimitSeconds", "30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("flashMessage", "Quiz created."));

        verify(creatorQuizFacade).create(any());
        verifyNoMoreInteractions(creatorQuizFacade);
    }

    @Test
    void create_validationError_rendersLayoutViewName() throws Exception {
        mvc.perform(post("/creator/quizzes")
                        .param("title", "")
                        .param("description", "D"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/layout"));

        verify(creatorQuizFacade, never()).create(any());
        verifyNoMoreInteractions(creatorQuizFacade);
    }

    @Test
    void create_validationError_exposesLayoutModelAttributes() throws Exception {
        mvc.perform(post("/creator/quizzes")
                        .param("title", "")
                        .param("description", "D"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("title", "contentTemplate", "contentFragment"));

        verify(creatorQuizFacade, never()).create(any());
        verifyNoMoreInteractions(creatorQuizFacade);
    }
}
