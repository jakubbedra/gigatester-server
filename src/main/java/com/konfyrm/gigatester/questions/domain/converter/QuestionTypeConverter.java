package com.konfyrm.gigatester.questions.domain.converter;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;

import java.util.Map;
import java.util.Optional;

public class QuestionTypeConverter {

    private static final Map<QuestionType, TesterEntityType> QUESTION_TYPE_TO_TESTER_ENTITY_TYPE = Map.of(
            QuestionType.OPEN, TesterEntityType.OPEN_QUESTION,
            QuestionType.CLOSED, TesterEntityType.CLOSED_QUESTION,
            QuestionType.STATEMENT, TesterEntityType.STATEMENT_QUESTION
    );

    private static final Map<TesterEntityType, QuestionType> TESTER_ENTITY_TYPE_TO_QUESTION_TYPE = Map.of(
            TesterEntityType.OPEN_QUESTION, QuestionType.OPEN,
            TesterEntityType.CLOSED_QUESTION, QuestionType.CLOSED,
            TesterEntityType.STATEMENT_QUESTION, QuestionType.STATEMENT
    );

    private QuestionTypeConverter() {
        throw new IllegalStateException("Utility class should not be instantiated.");
    }

    public static TesterEntityType toEntityType(QuestionType questionType) {
        return Optional.ofNullable(QUESTION_TYPE_TO_TESTER_ENTITY_TYPE.get(questionType)).orElseThrow(() -> new IllegalArgumentException("Unsupported question type: " + questionType));
    }

    public static QuestionType toQuestionType(TesterEntityType entityType) {
        return Optional.ofNullable(TESTER_ENTITY_TYPE_TO_QUESTION_TYPE.get(entityType)).orElseThrow(() -> new IllegalArgumentException("Unsupported entity type: " + entityType));
    }

}
