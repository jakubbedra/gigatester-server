package com.konfyrm.gigatester.crosswords.domain.converter;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CrosswordResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CrosswordTermResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CrosswordsResponse;
import com.konfyrm.gigatester.crosswords.domain.entity.Crossword;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordTerm;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrosswordConverter {

    public Crossword toEntity(CrosswordRequest request) {
        List<CrosswordTerm> terms = request.getTerms() == null ? List.of() :
                request.getTerms().stream()
                        .map(t -> CrosswordTerm.builder()
                                .term(t.getTerm())
                                .clue(t.getClue())
                                .clueType(t.getClueType())
                                .build())
                        .toList();

        return Crossword.builder()
                .name(request.getName())
                .terms(terms)
                .build();
    }

    public CrosswordResponse toResponse(Crossword crossword) {
        List<CrosswordTermResponse> terms = crossword.getTerms() == null ? List.of() :
                crossword.getTerms().stream()
                        .map(t -> CrosswordTermResponse.builder()
                                .id(t.getId())
                                .term(t.getTerm())
                                .clue(t.getClue())
                                .clueType(t.getClueType())
                                .build())
                        .toList();

        return CrosswordResponse.builder()
                .id(crossword.getId())
                .name(crossword.getName())
                .terms(terms)
                .build();
    }

    public CrosswordsResponse toResponse(List<Crossword> crosswords) {
        return new CrosswordsResponse(crosswords.stream()
                .map(c -> CrosswordsResponse.CrosswordSummaryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .termCount(c.getTerms() == null ? 0 : c.getTerms().size())
                        .build())
                .toList());
    }

}
