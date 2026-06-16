package com.konfyrm.gigatester.tests.domain.converter;

import com.konfyrm.gigatester.questions.domain.converter.QuestionTypeConverter;
import com.konfyrm.gigatester.tests.domain.dto.response.TestStateResponse;
import com.konfyrm.gigatester.tests.domain.entity.TestMode;
import com.konfyrm.gigatester.tests.domain.entity.TestState;
import org.springframework.stereotype.Component;

@Component
public class TestStateConverter {

    public TestStateResponse toResponse(TestState testState) {
        TestStateResponse.TestStateResponseBuilder builder = TestStateResponse.builder()
                .id(testState.getId())
                .testName(testState.getTest().getName())
                .questions(testState.getQuestions().stream().map(q -> TestStateResponse.QuestionStateSummaryResponse.builder()
                        .id(q.getId())
                        .questionId(q.getQuestion().getId())
                        .questionType(QuestionTypeConverter.toQuestionType(q.getQuestion().getType()))
                        .build()
                ).toList())
                .closedQuestionsCount(testState.getClosedQuestionsCount())
                .openQuestionsCount(testState.getOpenQuestionsCount())
                .termDefinitionQuestionsCount(testState.getTermDefinitionQuestionsCount())
                .currentQuestionsCount(getCurrentQuestionsCount(testState))
                .currentQuestionIndex(testState.getCurrentQuestionIndex())
                .mode(TestModeToDtoConverter.toDto(testState.getMode()))
                .displayType(TestDisplayTypeToDtoConverter.toDto(testState.getDisplayType()))
                .executionState(TestExecutionStateToDtoConverter.toDto(testState.getExecutionState()));
        if (testState.getPassingPercentage() != null) {
            builder.passingPercentage(testState.getPassingPercentage());
        }
        return builder.build();
    }

    private int getCurrentQuestionsCount(TestState testState) {
        if (testState.getMode() == TestMode.EXAM) {
            return testState.getClosedQuestionsCount() + testState.getOpenQuestionsCount() + testState.getTermDefinitionQuestionsCount();
        }
        return testState.getQuestions().size();
    }

}