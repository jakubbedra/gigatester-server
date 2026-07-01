package com.konfyrm.gigatester.subjects.domain.dto.response;

import com.konfyrm.gigatester.comments.domain.dto.response.CommentResponse;
import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {

    private UUID id;

    private String name;

    private String description;

    private double difficulty;

    private List<UUID> tests;

    private List<UUID> crosswords;

    private List<CommentResponse> comments;

    private List<UserResponse> authors;

}
