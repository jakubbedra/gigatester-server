package com.konfyrm.gigatester.graphs.domain.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphRequest {
    private String name;
    private String graphJson;
}
