package com.konfyrm.gigatester.tests.domain.dto.request;

import com.konfyrm.gigatester.tests.domain.dto.enums.NavigateActionDto;
import jakarta.annotation.Nonnull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestStateUpdateRequest {

    @Nonnull
    private NavigateActionDto action;

}
