package com.konfyrm.gigatester.subjects.domain.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectGroupResponse {

    private UUID id;

    private String name;

    private List<UUID> subjects;

}
