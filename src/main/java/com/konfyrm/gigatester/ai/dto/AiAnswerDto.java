package com.konfyrm.gigatester.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAnswerDto {
    private String text;
    private boolean correct;
}
