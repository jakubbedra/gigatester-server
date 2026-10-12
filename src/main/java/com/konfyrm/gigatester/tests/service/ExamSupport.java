package com.konfyrm.gigatester.tests.service;

import com.konfyrm.gigatester.questions.domain.dto.enums.GradingRule;
import com.konfyrm.gigatester.questions.domain.entity.ClosedQuestion;
import com.konfyrm.gigatester.questions.domain.entity.OpenQuestion;
import com.konfyrm.gigatester.questions.domain.entity.StatementQuestion;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.domain.entity.TestState;

import java.time.Instant;

/** Small helpers shared by the scheduled-exam flow (state handling, converters, grading). */
public final class ExamSupport {

    private ExamSupport() { }

    /** Results and review of a scheduled exam unlock once the window has closed for everyone. */
    public static boolean reviewAvailable(Test test) {
        return test.isScheduledExam() && test.getWindowEnd() != null && !Instant.now().isBefore(test.getWindowEnd());
    }

    public static boolean isManual(QuestionState questionState) {
        return questionState.getQuestion() instanceof OpenQuestion open
                && open.getGradingRulesHash() != null
                && open.getGradingRulesHash().indexOf(GradingRule.MANUAL.getHash()) >= 0;
    }

    public static boolean isGraded(QuestionState questionState) {
        return Boolean.TRUE.equals(questionState.getGradedByStaff());
    }

    public static double maxScore(QuestionState questionState) {
        return switch (questionState.getQuestion()) {
            case ClosedQuestion q -> q.getPoints() != null ? q.getPoints() : 0.0;
            case OpenQuestion q -> q.getPoints() != null ? q.getPoints() : 0.0;
            case StatementQuestion q -> q.getPoints() != null ? q.getPoints() : 0.0;
            default -> 0.0;
        };
    }

    public static int pendingManualCount(TestState state) {
        return (int) state.getQuestions().stream().filter(q -> isManual(q) && !isGraded(q)).count();
    }
}
