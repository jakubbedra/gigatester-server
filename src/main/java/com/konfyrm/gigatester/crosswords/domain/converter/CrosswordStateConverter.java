package com.konfyrm.gigatester.crosswords.domain.converter;

import com.konfyrm.gigatester.crosswords.domain.dto.enums.DirectionDto;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CrosswordPlayerResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CrosswordStateClueResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CrosswordStateResponse;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordPlayer;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordStateTerm;
import com.konfyrm.gigatester.crosswords.domain.entity.enums.Direction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrosswordStateConverter {

    public CrosswordStateResponse toResponse(CrosswordState state) {
        return CrosswordStateResponse.builder()
                .id(state.getId())
                .currentGrid(state.getCurrentGrid())
                .width(state.getWidth())
                .height(state.getHeight())
                .players(toPlayerResponses(state.getPlayers()))
                .clues(toClueResponses(state.getTerms()))
                .build();
    }

    private List<CrosswordPlayerResponse> toPlayerResponses(List<CrosswordPlayer> players) {
        if (players == null) return List.of();
        return players.stream()
                .map(p -> CrosswordPlayerResponse.builder()
                        .id(p.getId())
                        .handLetters(p.getHandLetters())
                        .points(p.getPoints())
                        .current(p.isCurrent())
                        .bot(p.isBot())
                        .build())
                .toList();
    }

    private List<CrosswordStateClueResponse> toClueResponses(List<CrosswordStateTerm> terms) {
        if (terms == null) return List.of();
        return terms.stream()
                .map(t -> CrosswordStateClueResponse.builder()
                        .id(t.getId())
                        .clue(t.getCrosswordTerm().getClue())
                        .clueType(t.getCrosswordTerm().getClueType())
                        .row(t.getRow())
                        .column(t.getColumn())
                        .direction(t.getDirection() == Direction.ACROSS ? DirectionDto.ACROSS : DirectionDto.DOWN)
                        .build())
                .toList();
    }

}
