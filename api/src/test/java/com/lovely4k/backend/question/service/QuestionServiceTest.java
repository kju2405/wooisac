package com.lovely4k.backend.question.service;

import com.lovely4k.TestData;

import com.lovely4k.backend.question.Question;
import com.lovely4k.backend.question.QuestionForm;
import com.lovely4k.backend.question.QuestionFormType;
import com.lovely4k.backend.question.repository.QuestionFormRepository;
import com.lovely4k.backend.question.repository.QuestionRepository;
import com.lovely4k.backend.question.service.request.CreateQuestionFormServiceRequest;
import com.lovely4k.backend.question.service.response.CreateQuestionFormResponse;
import com.lovely4k.backend.question.service.response.CreateQuestionResponse;
import jakarta.persistence.OptimisticLockException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest  {

    @Mock
    QuestionRepository questionRepository;

    @Mock
    QuestionValidator questionValidator;

    @Mock
    QuestionServiceSupporter questionServiceSupporter;

    @Mock
    QuestionFormRepository questionFormRepository;

    @InjectMocks
    QuestionService questionService;

    @DisplayName("질문 양식을 생성한다.")
    @Test
    void createQuestionForm() {
        // Given
        Long coupleId = 1L;
        Long userId = 1L;
        Long questionId = 1L;
        Long questionDay = 1L;
        CreateQuestionFormServiceRequest request = new CreateQuestionFormServiceRequest("test", "test1", "test2", null, null);

        Question mockQuestion = mock(Question.class);
        QuestionForm questionForm = TestData.questionForm(userId);
        given(mockQuestion.getQuestionForm()).willReturn(questionForm);
        given(mockQuestion.getId()).willReturn(questionId);
        CreateQuestionFormResponse expectedResponse = CreateQuestionFormResponse.from(mockQuestion);

        given(questionServiceSupporter.getQuestionDay(coupleId)).willReturn(questionDay);
        given(questionRepository.save(any(Question.class))).willReturn(mockQuestion);
        willDoNothing().given(questionValidator).validateCreateQuestionForm(coupleId, questionDay);

        // When
        CreateQuestionFormResponse actualResponse = questionService.createQuestionForm(request, coupleId, userId);

        // Then
        Assertions.assertThat(expectedResponse).isEqualTo(actualResponse);
        verify(questionValidator, times(1)).validateCreateQuestionForm(coupleId, questionDay);
    }

    @DisplayName("질문을 생성한다.")
    @Test
    void testCreateQuestion() {
        // Given
        Long coupleId = 1L;
        long questionDay = 1L;
        Question mockQuestion = mock(Question.class);
        QuestionForm questionForm = TestData.questionForm(1L);
        QuestionFormType type = QuestionFormType.SERVER;
        given(questionServiceSupporter.getQuestionDay(coupleId)).willReturn(questionDay);
        given(questionFormRepository.findByQuestionDayAndQuestionFormType(questionDay, type)).willReturn(Optional.of(questionForm));
        given(questionRepository.save(any(Question.class))).willReturn(mockQuestion);
        given(mockQuestion.getQuestionForm()).willReturn(questionForm);
        given(mockQuestion.getId()).willReturn(1L);

        CreateQuestionResponse expectedResponse = CreateQuestionResponse.from(mockQuestion);

        // When
        CreateQuestionResponse actualResponse = questionService.createQuestion(coupleId);

        // Then
        Assertions.assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(questionValidator, times(1)).validateCreateQuestion(coupleId, questionDay);
    }

    @DisplayName("질문에 대한 답변을 작성한다.")
    @Test
    void testUpdateQuestionAnswer() {
        // Given
        Long questionId = 1L;
        Long coupleId = 1L;
        Long loginUserId = 1L;
        Long boyId = 1L;
        int answer = 1;
        Question mockQuestion = mock(Question.class);

        // 성공적으로 Question을 조회할 수 있다고 가정
        given(questionRepository.findById(questionId)).willReturn(Optional.of(mockQuestion));
        given(questionServiceSupporter.getBoyId(coupleId)).willReturn(boyId);
        // When
        questionService.updateQuestionAnswer(questionId, coupleId, loginUserId, answer);

        // Then
        verify(mockQuestion, times(1)).updateAnswer(answer, boyId, loginUserId);
    }

    @DisplayName("updateQuestionAnswer 메서드 실패 (OptimisticLockException) 테스트")
    @Test
    void testUpdateQuestionAnswerOptimisticLockException() {
        // Given
        Long questionId = 1L;
        Long coupleId = 1L;
        Long loginUserId = 1L;
        int answer = 1;
        Long boyId = 1L;
        Question mockQuestion = mock(Question.class);

        // 성공적으로 Question을 조회할 수 있다고 가정
        given(questionRepository.findById(questionId)).willReturn(Optional.of(mockQuestion));
        given(questionServiceSupporter.getBoyId(coupleId)).willReturn(boyId);
        // updateAnswer 호출시 OptimisticLockException 발생
        doThrow(OptimisticLockException.class).when(mockQuestion).updateAnswer(answer, boyId, loginUserId);

        // When & Then
        Assertions.assertThatThrownBy(() -> questionService.updateQuestionAnswer(questionId, coupleId, loginUserId, answer))
            .isInstanceOf(OptimisticLockException.class);
    }

}