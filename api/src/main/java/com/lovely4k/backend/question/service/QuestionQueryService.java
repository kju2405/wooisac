package com.lovely4k.backend.question.service;

import com.lovely4k.backend.member.Sex;
import com.lovely4k.backend.question.repository.QuestionQueryRepository;
import com.lovely4k.backend.question.repository.response.AnsweredQuestionResponse;
import com.lovely4k.backend.question.repository.response.DailyQuestionResponse;
import com.lovely4k.backend.question.repository.response.QuestionDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class QuestionQueryService {

    private final QuestionQueryRepository questionQueryRepository;

    public QuestionDetailsResponse findQuestionDetails(Long questionId, Long memberId, String sex) {
        return questionQueryRepository.findQuestionDetails(questionId, Sex.valueOf(sex), memberId);
    }

    public AnsweredQuestionResponse findAllAnsweredQuestionByCoupleId(Long id, Long coupleId, Integer limit) {
        return questionQueryRepository.findAnsweredQuestions(id ,coupleId, limit);
    }

    public DailyQuestionResponse findDailyQuestion(Long coupleId) {
        return questionQueryRepository.findDailyQuestion(coupleId);
    }
}