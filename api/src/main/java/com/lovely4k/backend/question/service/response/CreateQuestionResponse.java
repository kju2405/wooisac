package com.lovely4k.backend.question.service.response;

import com.lovely4k.backend.question.Question;
import com.lovely4k.backend.question.QuestionChoices;
import com.lovely4k.backend.question.QuestionForm;
import com.lovely4k.backend.question.QuestionFormType;

public record CreateQuestionResponse(
    long questionId,
    String questionContent,
    String firstChoice,
    String secondChoice,
    String thirdChoice,
    String fourthChoice,
    QuestionFormType questionFormType
) {
    public static CreateQuestionResponse from(Question question) {
        QuestionForm questionForm = question.getQuestionForm();
        QuestionChoices questionChoices = questionForm.getQuestionChoices();
        return new CreateQuestionResponse(
            question.getId(),
            questionForm.getQuestionContent(),
            questionChoices.getFirstChoice(),
            questionChoices.getSecondChoice(),
            questionChoices.getThirdChoice(),
            questionChoices.getFourthChoice(),
            questionForm.getQuestionFormType()
        );
    }
}