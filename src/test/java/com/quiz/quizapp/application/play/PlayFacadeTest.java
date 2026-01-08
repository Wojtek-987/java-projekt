package com.quiz.quizapp.application.play;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.quizapp.api.dto.RankingRowResponse;
import com.quiz.quizapp.domain.dto.QuestionForPlayDto;
import com.quiz.quizapp.domain.dto.SubmitAnswerDto;
import com.quiz.quizapp.domain.dto.SubmitAnswersCommand;
import com.quiz.quizapp.domain.dto.SubmitOutcome;
import com.quiz.quizapp.domain.entity.AttemptEntity;
import com.quiz.quizapp.domain.entity.QuizEntity;
import com.quiz.quizapp.domain.service.AttemptReadService;
import com.quiz.quizapp.domain.service.AttemptService;
import com.quiz.quizapp.domain.service.GameplayService;
import com.quiz.quizapp.domain.service.QuizReadService;
import com.quiz.quizapp.domain.service.RankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class PlayFacadeTest {

    @Mock
    private QuizReadService quizReadService;

    @Mock
    private AttemptReadService attemptReadService;

    @Mock
    private AttemptService attemptService;

    @Mock
    private GameplayService gameplayService;

    @Mock
    private RankingService rankingService;

    @InjectMocks
    private PlayFacade facade = new PlayFacade(
            quizReadService,
            attemptReadService,
            attemptService,
            gameplayService,
            rankingService,
            new ObjectMapper()
    );

    @BeforeEach
    void setUp() {
        facade = new PlayFacade(
                quizReadService,
                attemptReadService,
                attemptService,
                gameplayService,
                rankingService,
                new ObjectMapper()
        );
    }

    @Test
    void listPlayableQuizzes_usesFixedPageRequest() {
        var quizzes = List.of(new QuizEntity("T1", "D1"), new QuizEntity("T2", "D2"));
        when(quizReadService.list(any()))
                .thenReturn(new PageImpl<>(quizzes, PageRequest.of(0, 50), quizzes.size()));

        facade.listPlayableQuizzes();

        verify(quizReadService).list(PageRequest.of(0, 50));
        verifyNoMoreInteractions(quizReadService);
    }

    @Test
    void listPlayableQuizzes_returnsListSizeMatchingRepositoryPageContent() {
        var quizzes = List.of(new QuizEntity("T1", "D1"), new QuizEntity("T2", "D2"));
        when(quizReadService.list(any()))
                .thenReturn(new PageImpl<>(quizzes, PageRequest.of(0, 50), quizzes.size()));

        List<?> out = facade.listPlayableQuizzes();

        assertThat(out).hasSize(2);
    }

    @Test
    void getStartViewModel_containsQuizReference() {
        QuizEntity quiz = new QuizEntity("Title", "Desc");
        when(quizReadService.getOrThrow(7L)).thenReturn(quiz);

        Map<String, Object> vm = facade.getStartViewModel(7L);

        assertThat(vm).containsEntry("quiz", quiz);
    }

    @Test
    void startAttempt_returnsAttemptIdFromAttemptService() {
        when(attemptService.start(eq(5L), eq("nick")))
                .thenReturn(new com.quiz.quizapp.domain.dto.AttemptInfo(123L, 5L, "nick", 0, null, null));

        long id = facade.startAttempt(5L, "nick");

        assertThat(id).isEqualTo(123L);
    }

    @Test
    void getAttemptViewModel_containsAttemptReference() throws Exception {
        QuizEntity quiz = new QuizEntity("QuizTitle", "Desc");
        setEntityId(quiz, 77L);
        quiz.setRandomiseAnswers(false);
        quiz.setNegativePointsEnabled(true);
        quiz.setTimeLimitSeconds(30);

        AttemptEntity attempt = new AttemptEntity(quiz, "nick");
        when(attemptReadService.getWithQuizOrThrow(1L)).thenReturn(attempt);

        when(gameplayService.questionsForAttempt(1L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SINGLE_CHOICE", "P1", 1, "[\"A\",\"B\"]")
        ));

        Map<String, Object> vm = facade.getAttemptViewModel(1L);

        assertThat(vm.get("attempt")).isSameAs(attempt);
    }

    @Test
    void getAttemptViewModel_containsQuizReference() throws Exception {
        QuizEntity quiz = new QuizEntity("QuizTitle", "Desc");
        setEntityId(quiz, 77L);
        quiz.setRandomiseAnswers(false);
        quiz.setNegativePointsEnabled(true);
        quiz.setTimeLimitSeconds(30);

        AttemptEntity attempt = new AttemptEntity(quiz, "nick");
        when(attemptReadService.getWithQuizOrThrow(1L)).thenReturn(attempt);

        when(gameplayService.questionsForAttempt(1L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SINGLE_CHOICE", "P1", 1, "[\"A\",\"B\"]")
        ));

        Map<String, Object> vm = facade.getAttemptViewModel(1L);

        assertThat(vm.get("quiz")).isSameAs(quiz);
    }

    @Test
    void getAttemptViewModel_containsQuizTitle() throws Exception {
        QuizEntity quiz = new QuizEntity("QuizTitle", "Desc");
        setEntityId(quiz, 77L);
        quiz.setRandomiseAnswers(false);
        quiz.setNegativePointsEnabled(true);
        quiz.setTimeLimitSeconds(30);

        AttemptEntity attempt = new AttemptEntity(quiz, "nick");
        when(attemptReadService.getWithQuizOrThrow(1L)).thenReturn(attempt);

        when(gameplayService.questionsForAttempt(1L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SINGLE_CHOICE", "P1", 1, "[\"A\",\"B\"]")
        ));

        Map<String, Object> vm = facade.getAttemptViewModel(1L);

        assertThat(vm.get("quizTitle")).isEqualTo("QuizTitle");
    }

    @Test
    void getAttemptViewModel_containsTimeLimitSeconds() throws Exception {
        QuizEntity quiz = new QuizEntity("QuizTitle", "Desc");
        setEntityId(quiz, 77L);
        quiz.setRandomiseAnswers(false);
        quiz.setNegativePointsEnabled(true);
        quiz.setTimeLimitSeconds(30);

        AttemptEntity attempt = new AttemptEntity(quiz, "nick");
        when(attemptReadService.getWithQuizOrThrow(1L)).thenReturn(attempt);

        when(gameplayService.questionsForAttempt(1L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SINGLE_CHOICE", "P1", 1, "[\"A\",\"B\"]")
        ));

        Map<String, Object> vm = facade.getAttemptViewModel(1L);

        assertThat(vm.get("timeLimitSeconds")).isEqualTo(30);
    }

    @Test
    void getAttemptViewModel_containsNegativePointsEnabled() throws Exception {
        QuizEntity quiz = new QuizEntity("QuizTitle", "Desc");
        setEntityId(quiz, 77L);
        quiz.setRandomiseAnswers(false);
        quiz.setNegativePointsEnabled(true);
        quiz.setTimeLimitSeconds(30);

        AttemptEntity attempt = new AttemptEntity(quiz, "nick");
        when(attemptReadService.getWithQuizOrThrow(1L)).thenReturn(attempt);

        when(gameplayService.questionsForAttempt(1L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SINGLE_CHOICE", "P1", 1, "[\"A\",\"B\"]")
        ));

        Map<String, Object> vm = facade.getAttemptViewModel(1L);

        assertThat(vm.get("negativePointsEnabled")).isEqualTo(true);
    }

    @Test
    void getAttemptViewModel_buildsOptionsMapForQuestionWithOptionsJson() throws Exception {
        QuizEntity quiz = new QuizEntity("QuizTitle", "Desc");
        setEntityId(quiz, 77L);

        AttemptEntity attempt = new AttemptEntity(quiz, "nick");
        when(attemptReadService.getWithQuizOrThrow(1L)).thenReturn(attempt);

        when(gameplayService.questionsForAttempt(1L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SINGLE_CHOICE", "P1", 1, "[\"A\",\"B\"]"),
                new QuestionForPlayDto(12L, "SHORT_ANSWER", "P2", 1, null)
        ));

        Map<String, Object> vm = facade.getAttemptViewModel(1L);

        @SuppressWarnings("unchecked")
        Map<Long, List<String>> options = (Map<Long, List<String>>) vm.get("optionsByQuestionId");
        assertThat(options.get(11L)).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void getAttemptViewModel_doesNotCreateOptionsEntryForQuestionWithoutOptionsJson() throws Exception {
        QuizEntity quiz = new QuizEntity("QuizTitle", "Desc");
        setEntityId(quiz, 77L);

        AttemptEntity attempt = new AttemptEntity(quiz, "nick");
        when(attemptReadService.getWithQuizOrThrow(1L)).thenReturn(attempt);

        when(gameplayService.questionsForAttempt(1L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SHORT_ANSWER", "P2", 1, null)
        ));

        Map<String, Object> vm = facade.getAttemptViewModel(1L);

        @SuppressWarnings("unchecked")
        Map<Long, List<String>> options = (Map<Long, List<String>>) vm.get("optionsByQuestionId");
        assertThat(options).doesNotContainKey(11L);
    }

    @Test
    void submitAttempt_throwsIncompleteAnswersWhenAnyQuestionAnswerMissing() throws Exception {
        QuizEntity quiz = new QuizEntity("T", "D");
        setEntityId(quiz, 77L);
        AttemptEntity attempt = new AttemptEntity(quiz, "nick");

        when(attemptReadService.getWithQuizOrThrow(1L)).thenReturn(attempt);
        when(gameplayService.questionsForAttempt(1L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SHORT_ANSWER", "P", 1, null)
        ));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        assertThatThrownBy(() -> facade.submitAttempt(1L, params))
                .isInstanceOf(PlayFacade.IncompleteAnswersException.class)
                .hasMessageContaining("You must answer every question.");
    }

    @Test
    void submitAttempt_doesNotCallSubmitWhenIncompleteAnswers() throws Exception {
        QuizEntity quiz = new QuizEntity("T", "D");
        setEntityId(quiz, 77L);
        AttemptEntity attempt = new AttemptEntity(quiz, "nick");

        when(attemptReadService.getWithQuizOrThrow(1L)).thenReturn(attempt);
        when(gameplayService.questionsForAttempt(1L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SHORT_ANSWER", "P", 1, null)
        ));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        assertThatThrownBy(() -> facade.submitAttempt(1L, params))
                .isInstanceOf(PlayFacade.IncompleteAnswersException.class);

        verify(gameplayService, never()).submitAndFinish(anyLong(), any(SubmitAnswersCommand.class));
    }

    @Test
    void submitAttempt_returnsQuizIdFromAttemptQuiz() throws Exception {
        QuizEntity quiz = new QuizEntity("T", "D");
        setEntityId(quiz, 77L);
        AttemptEntity attempt = new AttemptEntity(quiz, "nick");

        when(attemptReadService.getWithQuizOrThrow(5L)).thenReturn(attempt);
        when(gameplayService.questionsForAttempt(5L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SHORT_ANSWER", "P1", 1, null),
                new QuestionForPlayDto(12L, "TRUE_FALSE", "P2", 1, null)
        ));
        when(gameplayService.submitAndFinish(eq(5L), any(SubmitAnswersCommand.class)))
                .thenReturn(new SubmitOutcome(5L, 42));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("q_11", "hello");
        params.add("q_12", "true");

        PlayFacade.SubmitOutcome out = facade.submitAttempt(5L, params);

        assertThat(out.quizId()).isEqualTo(77L);
    }

    @Test
    void submitAttempt_returnsTotalScoreFromGameplayOutcome() throws Exception {
        QuizEntity quiz = new QuizEntity("T", "D");
        setEntityId(quiz, 77L);
        AttemptEntity attempt = new AttemptEntity(quiz, "nick");

        when(attemptReadService.getWithQuizOrThrow(5L)).thenReturn(attempt);
        when(gameplayService.questionsForAttempt(5L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SHORT_ANSWER", "P1", 1, null),
                new QuestionForPlayDto(12L, "TRUE_FALSE", "P2", 1, null)
        ));
        when(gameplayService.submitAndFinish(eq(5L), any(SubmitAnswersCommand.class)))
                .thenReturn(new SubmitOutcome(5L, 42));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("q_11", "hello");
        params.add("q_12", "true");

        PlayFacade.SubmitOutcome out = facade.submitAttempt(5L, params);

        assertThat(out.totalScore()).isEqualTo(42);
    }

    @Test
    void submitAttempt_buildsCommandWithAnswersInQuestionOrder() throws Exception {
        QuizEntity quiz = new QuizEntity("T", "D");
        setEntityId(quiz, 77L);
        AttemptEntity attempt = new AttemptEntity(quiz, "nick");

        when(attemptReadService.getWithQuizOrThrow(5L)).thenReturn(attempt);
        when(gameplayService.questionsForAttempt(5L)).thenReturn(List.of(
                new QuestionForPlayDto(11L, "SHORT_ANSWER", "P1", 1, null),
                new QuestionForPlayDto(12L, "TRUE_FALSE", "P2", 1, null)
        ));
        when(gameplayService.submitAndFinish(eq(5L), any(SubmitAnswersCommand.class)))
                .thenReturn(new SubmitOutcome(5L, 42));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("q_11", "hello");
        params.add("q_12", "true");

        facade.submitAttempt(5L, params);

        ArgumentCaptor<SubmitAnswersCommand> captor = ArgumentCaptor.forClass(SubmitAnswersCommand.class);
        verify(gameplayService).submitAndFinish(eq(5L), captor.capture());

        SubmitAnswersCommand cmd = captor.getValue();
        assertThat(cmd.answers()).extracting(SubmitAnswerDto::questionId).containsExactly(11L, 12L);
    }

    @Test
    void getRankingViewModel_containsQuizReference() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(9L)).thenReturn(quiz);
        when(rankingService.topForQuiz(9L, 10)).thenReturn(List.of());

        Map<String, Object> vm = facade.getRankingViewModel(9L, 10);

        assertThat(vm.get("quiz")).isSameAs(quiz);
    }

    @Test
    void getRankingViewModel_containsRowsReference() {
        QuizEntity quiz = new QuizEntity("T", "D");
        when(quizReadService.getOrThrow(9L)).thenReturn(quiz);

        List<RankingRowResponse> rows = List.of(
                new RankingRowResponse("A", 10),
                new RankingRowResponse("B", 5)
        );
        when(rankingService.topForQuiz(9L, 10)).thenReturn(rows);

        Map<String, Object> vm = facade.getRankingViewModel(9L, 10);

        assertThat(vm.get("rows")).isSameAs(rows);
    }

    private static void setEntityId(Object entity, long id) throws Exception {
        Field f = entity.getClass().getDeclaredField("id");
        f.setAccessible(true);
        f.set(entity, id);
    }
}
