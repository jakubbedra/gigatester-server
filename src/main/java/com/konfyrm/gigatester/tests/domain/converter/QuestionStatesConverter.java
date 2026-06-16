package com.konfyrm.gigatester.tests.domain.converter;

import com.konfyrm.gigatester.common.domain.TesterEntityType;
import com.konfyrm.gigatester.questions.domain.converter.QuestionTypeConverter;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.questions.domain.entity.Question;
import com.konfyrm.gigatester.questions.repository.QuestionRepository;
import com.konfyrm.gigatester.tests.domain.dto.request.QuestionStateRequest;
import com.konfyrm.gigatester.tests.domain.dto.response.QuestionStateResponse;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class QuestionStatesConverter {

    private final Map<QuestionType, QuestionStateConverter> questionTypeToConverter;

    private final Map<TesterEntityType, QuestionStateConverter> entityTypeToConverter;

    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionStatesConverter(
            @Qualifier(OpenQuestionStateConverter.QUALIFIER) QuestionStateConverter openQuestionStateConverter,
            @Qualifier(ClosedQuestionStateConverter.QUALIFIER) QuestionStateConverter closedQuestionStateConverter,
            @Qualifier(TermDefinitionQuestionStateConverter.QUALIFIER) QuestionStateConverter termDefinitionQuestionStateConverter,
            @Qualifier(StatementQuestionStateConverter.QUALIFIER) StatementQuestionStateConverter statementQuestionStateConverter,
            QuestionRepository questionRepository
    ) {
        questionTypeToConverter = Map.of(
                QuestionType.OPEN, openQuestionStateConverter,
                QuestionType.CLOSED, closedQuestionStateConverter,
                QuestionType.TERM_DEFINITION, termDefinitionQuestionStateConverter,
                QuestionType.STATEMENT, statementQuestionStateConverter
        );
        entityTypeToConverter = Map.of(
                TesterEntityType.OPEN_QUESTION, openQuestionStateConverter,
                TesterEntityType.CLOSED_QUESTION, closedQuestionStateConverter,
                TesterEntityType.TERM_DEFINITION_QUESTION, termDefinitionQuestionStateConverter,
                TesterEntityType.STATEMENT_QUESTION, statementQuestionStateConverter
        );
        this.questionRepository = questionRepository;
    }

    public QuestionStateResponse toResponse(QuestionState questionState) {
        return Optional.ofNullable(entityTypeToConverter.get(questionState.getQuestion().getType()))
                .orElseThrow(() -> new IllegalArgumentException("Unknown question type: " + questionState.getQuestion().getType()))
                .toResponseBuilder(questionState)
                .id(questionState.getId())
                .questionId(questionState.getQuestion().getId())
                .questionType(QuestionTypeConverter.toQuestionType(questionState.getQuestion().getType()))
                .score(questionState.getScore())
                .answered(questionState.isAnswered())
                .build();
    }

    public List<QuestionStateResponse> toResponses(List<QuestionState> questionStates) {
        return questionStates.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public QuestionState toEntity(QuestionStateRequest request) {
        Optional<Question> questionOptional = questionRepository.findById(request.getQuestionId());
        if (questionOptional.isEmpty()) {
            throw new IllegalArgumentException("Unknown question: " + request.getQuestionId());
        }
        Question question = questionOptional.get();
        return Optional.ofNullable(questionTypeToConverter.get(request.getQuestionType()))
                .orElseThrow(() -> new IllegalArgumentException("Unknown question type: " + request.getQuestionType()))
                .toEntityBuilder(request)
                .question(question)
                .answered(request.isAnswered())
                .build();
    }

    public List<QuestionState> toEntities(List<QuestionStateRequest> requests) {
        return requests.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

}
