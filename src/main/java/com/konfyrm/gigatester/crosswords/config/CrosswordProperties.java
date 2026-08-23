package com.konfyrm.gigatester.crosswords.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "crossword")
@Getter
@Setter
public class CrosswordProperties {

    private int maxWordLimit = 30;
}
