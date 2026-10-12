package com.konfyrm.gigatester.coding.domain.dto.request;

import lombok.*;

import java.util.List;

/** The code plus what it actually printed for each test, in test order. The server does the comparing. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitCodingRequest {

    private String code;
    private List<String> outputs;

}
