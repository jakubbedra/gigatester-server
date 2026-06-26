package com.konfyrm.gigatester.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiQuestionDto {
    private String type; // "CLOSED_QUESTION" or "OPEN_QUESTION"
    private boolean multipleChoice;
    private String questionText;
    private List<AiAnswerDto> answers; // for closed questions
    private String openAnswer;         // for open questions
}
