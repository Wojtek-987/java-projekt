package com.quiz.quizapp.web;

import com.quiz.quizapp.application.creator.CreatorQuestionFacade;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.web.dto.CreateQuestionForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.ViewResolver;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

@WebMvcTest(controllers = CreatorQuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.thymeleaf.check-template-location=false"
})
@Import(CreatorQuestionControllerWebMvcTest.NoThymeleafViews.class)
class CreatorQuestionControllerWebMvcTest {

    @TestConfiguration
    static class NoThymeleafViews {

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        ViewResolver testViewResolver() {
            return (viewName, locale) -> (model, request, response) -> response.setStatus(200);
        }
    }

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CreatorQuestionFacade creatorQuestionFacade;

    @Test
    void list_rendersLayoutViewName() throws Exception {
        when(creatorQuestionFacade.getListViewModel(1L)).thenReturn(Map.of(
                "quiz", new QuizEntity("T", "D"),
                "title", "Creator • Questions",
                "contentTemplate", "creator/questions",
                "contentFragment", "content"
        ));

        mvc.perform(get("/creator/quizzes/1/questions"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/layout"));

        verify(creatorQuestionFacade).getListViewModel(1L);
        verifyNoMoreInteractions(creatorQuestionFacade);
    }

    @Test
    void list_exposesExpectedModelAttributes() throws Exception {
        when(creatorQuestionFacade.getListViewModel(1L)).thenReturn(Map.of(
                "quiz", new QuizEntity("T", "D"),
                "title", "Creator • Questions",
                "contentTemplate", "creator/questions",
                "contentFragment", "content"
        ));

        mvc.perform(get("/creator/quizzes/1/questions"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("quiz", "title", "contentTemplate", "contentFragment"));

        verify(creatorQuestionFacade).getListViewModel(1L);
        verifyNoMoreInteractions(creatorQuestionFacade);
    }

    @Test
    void newForm_rendersLayoutViewName() throws Exception {
        when(creatorQuestionFacade.getNewFormViewModel(1L)).thenReturn(Map.of(
                "quiz", new QuizEntity("T", "D"),
                "form", new CreateQuestionForm(),
                "title", "Creator • New question",
                "contentTemplate", "creator/question-new",
                "contentFragment", "content"
        ));

        mvc.perform(get("/creator/quizzes/1/questions/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/layout"));

        verify(creatorQuestionFacade).getNewFormViewModel(1L);
        verifyNoMoreInteractions(creatorQuestionFacade);
    }

    @Test
    void newForm_exposesExpectedModelAttributes() throws Exception {
        when(creatorQuestionFacade.getNewFormViewModel(1L)).thenReturn(Map.of(
                "quiz", new QuizEntity("T", "D"),
                "form", new CreateQuestionForm(),
                "title", "Creator • New question",
                "contentTemplate", "creator/question-new",
                "contentFragment", "content"
        ));

        mvc.perform(get("/creator/quizzes/1/questions/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("quiz", "form", "title", "contentTemplate", "contentFragment"));

        verify(creatorQuestionFacade).getNewFormViewModel(1L);
        verifyNoMoreInteractions(creatorQuestionFacade);
    }

    @Test
    void create_success_redirectsToList() throws Exception {
        mvc.perform(post("/creator/quizzes/1/questions")
                        .param("quizId", "1")
                        .param("type", "SINGLE_CHOICE")
                        .param("prompt", "P")
                        .param("points", "1")
                        .param("answerKeyJson", "{\"value\":\"A\"}")
                        .param("optionsJson", "[\"A\",\"B\"]"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/creator/quizzes/1/questions"));

        verify(creatorQuestionFacade).create(eq(1L), any(CreateQuestionForm.class));
        verifyNoMoreInteractions(creatorQuestionFacade);
    }

    @Test
    void create_success_setsFlashMessage() throws Exception {
        mvc.perform(post("/creator/quizzes/1/questions")
                        .param("quizId", "1")
                        .param("type", "SINGLE_CHOICE")
                        .param("prompt", "P")
                        .param("points", "1")
                        .param("answerKeyJson", "{\"value\":\"A\"}")
                        .param("optionsJson", "[\"A\",\"B\"]"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("flashMessage", "Question created."));

        verify(creatorQuestionFacade).create(eq(1L), any(CreateQuestionForm.class));
        verifyNoMoreInteractions(creatorQuestionFacade);
    }

    @Test
    void create_validationError_rendersLayoutViewName() throws Exception {
        Map<String, Object> vm = new HashMap<>();
        vm.put("quiz", new QuizEntity("T", "D"));
        vm.put("title", "Creator • New question");
        vm.put("contentTemplate", "creator/question-new");
        vm.put("contentFragment", "content");
        when(creatorQuestionFacade.getNewFormViewModel(eq(1L), any(CreateQuestionForm.class))).thenReturn(vm);

        mvc.perform(post("/creator/quizzes/1/questions")
                        .param("quizId", "1")
                        .param("type", "SINGLE_CHOICE")
                        .param("prompt", "")
                        .param("points", "1")
                        .param("answerKeyJson", "{\"value\":\"A\"}"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/layout"));

        verify(creatorQuestionFacade).getNewFormViewModel(eq(1L), any(CreateQuestionForm.class));
        verify(creatorQuestionFacade, never()).create(eq(1L), any(CreateQuestionForm.class));
        verifyNoMoreInteractions(creatorQuestionFacade);
    }

    @Test
    void create_validationError_exposesLayoutModelAttributes() throws Exception {
        Map<String, Object> vm = new HashMap<>();
        vm.put("quiz", new QuizEntity("T", "D"));
        vm.put("title", "Creator • New question");
        vm.put("contentTemplate", "creator/question-new");
        vm.put("contentFragment", "content");
        when(creatorQuestionFacade.getNewFormViewModel(eq(1L), any(CreateQuestionForm.class))).thenReturn(vm);

        mvc.perform(post("/creator/quizzes/1/questions")
                        .param("quizId", "1")
                        .param("type", "SINGLE_CHOICE")
                        .param("prompt", "")
                        .param("points", "1")
                        .param("answerKeyJson", "{\"value\":\"A\"}"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("title", "contentTemplate", "contentFragment"));

        verify(creatorQuestionFacade).getNewFormViewModel(eq(1L), any(CreateQuestionForm.class));
        verify(creatorQuestionFacade, never()).create(eq(1L), any(CreateQuestionForm.class));
        verifyNoMoreInteractions(creatorQuestionFacade);
    }

    @Test
    void create_invalidJson_rendersLayoutViewName() throws Exception {
        Map<String, Object> vm = new HashMap<>();
        vm.put("quiz", new QuizEntity("T", "D"));
        vm.put("title", "Creator • New question");
        vm.put("contentTemplate", "creator/question-new");
        vm.put("contentFragment", "content");
        when(creatorQuestionFacade.getNewFormViewModel(eq(1L), any(CreateQuestionForm.class))).thenReturn(vm);

        doThrow(new CreatorQuestionFacade.InvalidQuestionJsonException("", new IllegalArgumentException("bad json")))
                .when(creatorQuestionFacade).create(eq(1L), any(CreateQuestionForm.class));

        mvc.perform(post("/creator/quizzes/1/questions")
                        .param("quizId", "1")
                        .param("type", "SINGLE_CHOICE")
                        .param("prompt", "P")
                        .param("points", "1")
                        .param("answerKeyJson", "not-json"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/layout"));

        verify(creatorQuestionFacade).create(eq(1L), any(CreateQuestionForm.class));
        verify(creatorQuestionFacade).getNewFormViewModel(eq(1L), any(CreateQuestionForm.class));
        verifyNoMoreInteractions(creatorQuestionFacade);
    }

    @Test
    void create_invalidJson_exposesLayoutModelAttributes() throws Exception {
        Map<String, Object> vm = new HashMap<>();
        vm.put("quiz", new QuizEntity("T", "D"));
        vm.put("title", "Creator • New question");
        vm.put("contentTemplate", "creator/question-new");
        vm.put("contentFragment", "content");
        when(creatorQuestionFacade.getNewFormViewModel(eq(1L), any(CreateQuestionForm.class))).thenReturn(vm);

        doThrow(new CreatorQuestionFacade.InvalidQuestionJsonException("", new IllegalArgumentException("bad json")))
                .when(creatorQuestionFacade).create(eq(1L), any(CreateQuestionForm.class));

        mvc.perform(post("/creator/quizzes/1/questions")
                        .param("quizId", "1")
                        .param("type", "SINGLE_CHOICE")
                        .param("prompt", "P")
                        .param("points", "1")
                        .param("answerKeyJson", "not-json"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("title", "contentTemplate", "contentFragment"));

        verify(creatorQuestionFacade).create(eq(1L), any(CreateQuestionForm.class));
        verify(creatorQuestionFacade).getNewFormViewModel(eq(1L), any(CreateQuestionForm.class));
        verifyNoMoreInteractions(creatorQuestionFacade);
    }
}
