package com.konfyrm.gigatester.crosswords.domain.dto.response;

import com.konfyrm.gigatester.users.domain.dto.response.UserResponse;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrosswordResponse {

    private UUID id;

    private String name;

    private List<CrosswordTermResponse> terms;

    private List<UserResponse> authors;

}
