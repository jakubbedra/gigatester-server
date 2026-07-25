package com.konfyrm.gigatester.tests.domain.converter;

import com.konfyrm.gigatester.questions.domain.converter.QuestionTypeConverter;
import com.konfyrm.gigatester.questions.domain.entity.ClosedQuestion;
import com.konfyrm.gigatester.questions.domain.entity.OpenQuestion;
import com.konfyrm.gigatester.questions.domain.entity.StatementQuestion;
import com.konfyrm.gigatester.tests.domain.dto.response.TestStateResponse;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
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
                .currentQuestionsCount(getCurrentQuestionsCount(testState))
                .currentQuestionIndex(testState.getCurrentQuestionIndex())
                .mode(TestModeToDtoConverter.toDto(testState.getMode()))
                .displayType(TestDisplayTypeToDtoConverter.toDto(testState.getDisplayType()))
                .executionState(TestExecutionStateToDtoConverter.toDto(testState.getExecutionState()));
        if (testState.getPassingPercentage() != null) {
            builder.passingPercentage(testState.getPassingPercentage());
        }
        return builder.totalScore(testState.getQuestions().stream().mapToDouble(q -> q.getScore() != null ? q.getScore() : 0.0).sum())
                .maxScore(testState.getQuestions().stream().mapToDouble(this::getMaxScore).sum())
                .timeLimitEnabled(testState.isTimeLimitEnabled())
                .timeLimitMs(testState.getTimeLimitMs())
                .startTime(testState.getStartTime())
                .hideTagsDuringTest(Boolean.TRUE.equals(testState.getTest().getHideTagsDuringTest()))
                .build();
    }

    private double getMaxScore(QuestionState questionState) {
        return switch (questionState.getQuestion()) {
            case ClosedQuestion q -> q.getPoints() != null ? q.getPoints() : 0.0;
            case OpenQuestion q -> q.getPoints() != null ? q.getPoints() : 0.0;
            case StatementQuestion q -> q.getPoints() != null ? q.getPoints() : 0.0;
            default -> 0.0;
        };
    }

    private int getCurrentQuestionsCount(TestState testState) {
        if (testState.getMode() == TestMode.EXAM) {
            return testState.getClosedQuestionsCount() + testState.getOpenQuestionsCount();
        }
        return testState.getQuestions().size();
    }

}