package com.konfyrm.gigatester.subjects.domain.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRequest {

    private String name;

    private String description;

    private double difficulty;

    private List<UUID> tests;

    private List<UUID> crosswords;

}
