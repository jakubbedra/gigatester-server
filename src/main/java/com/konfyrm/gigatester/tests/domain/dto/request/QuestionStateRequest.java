package com.konfyrm.gigatester.tests.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "questionType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClosedQuestionStateRequest.class, name = "CLOSED"),
        @JsonSubTypes.Type(value = OpenQuestionStateRequest.class, name = "OPEN"),
        @JsonSubTypes.Type(value = StatementQuestionStateRequest.class, name = "STATEMENT")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class QuestionStateRequest {

    private QuestionType questionType;

    private UUID questionId;

    private boolean answered;

}