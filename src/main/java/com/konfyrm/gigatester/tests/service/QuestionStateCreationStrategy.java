package com.konfyrm.gigatester.tests.service;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.domain.entity.StatementQuestion;
import com.konfyrm.gigatester.questions.domain.entity.TermDefinitionQuestion;
import com.konfyrm.gigatester.tests.domain.entity.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public enum QuestionStateCreationStrategy {
    CLOSED {
        @Override
        public QuestionState createQuestionState(Question question) {
            return ClosedQuestionState.builder()
                    .question(question)
                    .selectedAnswerIds(Set.of())
                    .score(0.0)
                    .answered(false)
                    .build();
        }
    },
    OPEN {
        @Override
        public QuestionState createQuestionState(Question question) {
            return OpenQuestionState.builder()
                    .question(question)
                    .givenAnswer("")
                    .score(0.0)
                    .answered(false)
                    .build();
        }
    },
    TERM_DEFINITION {
        @Override
        public QuestionState createQuestionState(Question question) {
            if (question instanceof TermDefinitionQuestion termDefinitionQuestion) {
                return TermDefinitionQuestionState.builder()
                        .question(question)
                        .termDefinitions(new ArrayList<>(termDefinitionQuestion.getTermDefinitions().size()))
                        .score(0.0)
                        .answered(false)
                        .build();
            }
            throw new IllegalArgumentException("Question with id: " + question.getId() + " is not a TermDefinitionQuestion.");
        }
    }, STATEMENT {
        @Override
        public QuestionState createQuestionState(Question question) {
            if (question instanceof StatementQuestion statementQuestion) {
                return StatementQuestionState.builder()
                        .question(statementQuestion)
                        .answers(statementQuestion.getStatements().stream().map(s -> getNullBoolean()).toList())
                        .score(0.0)
                        .answered(false)
                        .build();
            }
            throw new IllegalArgumentException("Question with id: " + question.getId() + " is not a StatementQuestion.");
        }

        private Boolean getNullBoolean() {
            return null;
        }
    };

    public abstract QuestionState createQuestionState(Question question);

    private static final Map<TesterEntityType, QuestionStateCreationStrategy> STRATEGIES = Map.of(
            TesterEntityType.OPEN_QUESTION, QuestionStateCreationStrategy.OPEN,
            TesterEntityType.CLOSED_QUESTION, QuestionStateCreationStrategy.CLOSED,
            TesterEntityType.TERM_DEFINITION_QUESTION, QuestionStateCreationStrategy.TERM_DEFINITION,
            TesterEntityType.STATEMENT_QUESTION, QuestionStateCreationStrategy.STATEMENT
    );

    public static QuestionStateCreationStrategy getStrategy(TesterEntityType type) {
        return Optional.ofNullable(STRATEGIES.get(type)).orElseThrow(() -> new IllegalArgumentException("The following entity type has no question creation strategy: " + type));
    }

}
