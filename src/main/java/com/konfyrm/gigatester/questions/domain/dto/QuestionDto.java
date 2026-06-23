package com.konfyrm.gigatester.questions.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.konfyrm.gigatester.questions.domain.dto.enums.QuestionType;
import com.konfyrm.gigatester.questions.domain.entity.QuestionContent;
import com.konfyrm.gigatester.tags.dto.TagResponse;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClosedQuestionDto.class, name = "CLOSED"),
        @JsonSubTypes.Type(value = OpenQuestionDto.class, name = "OPEN"),
        @JsonSubTypes.Type(value = StatementQuestionDto.class, name = "STATEMENT")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDto {

    @Nullable
    private UUID id;

    @Nonnull
    private QuestionType type;

    @Nonnull
    private QuestionContentDto content;

    @Nullable
    private QuestionContent explanation;

    @Nullable
    private Integer contentProportions;

    @Nullable
    private Integer answerProportions;

    @Nullable
    private List<TagResponse> tags;

}