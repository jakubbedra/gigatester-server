package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordLetterRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CompletedWordResult;
import com.konfyrm.gigatester.crosswords.domain.dto.response.TurnCellResult;
import com.konfyrm.gigatester.crosswords.domain.dto.response.TurnResultResponse;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordPlayer;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordStateTerm;
import com.konfyrm.gigatester.crosswords.domain.entity.enums.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CrosswordTurnService {

    private static final Random RANDOM = new Random();

    public record TurnOutcome(CrosswordState state, TurnResultResponse result) {}

    public TurnOutcome processTurn(CrosswordState state, List<CrosswordLetterRequest> letters) {
        CrosswordPlayer human = findHuman(state);
        CrosswordPlayer bot = findBot(state);

        // ── 1. Validate human letters ──────────────────────────────────────
        List<TurnCellResult> humanResults = new ArrayList<>();

        Set<UUID> completeBefore = completeWords(state);

        for (CrosswordLetterRequest letter : letters) {
            int r = letter.getRow(), c = letter.getColumn();
            char played = Character.toUpperCase(letter.getC());
            boolean correct = state.currentAt(r, c) == CrosswordState.UNCOVERED_FIELD
                    && state.solutionAt(r, c) == played;

            if (correct) {
                state.setCurrentAt(r, c, played);
                human.setPoints(human.getPoints() + 1);
            } else {
                human.setPoints(Math.max(0, human.getPoints() - 1));
            }

            humanResults.add(TurnCellResult.builder()
                    .row(r).column(c).letter(played).correct(correct).build());
        }

        // Word-completion bonus for human
        Set<UUID> completeAfterHuman = completeWords(state);
        List<CompletedWordResult> humanCompletedWords = newlyCompleted(state, completeBefore, completeAfterHuman);
        int humanWordBonus = humanCompletedWords.stream().mapToInt(CompletedWordResult::getPoints).sum();
        human.setPoints(human.getPoints() + humanWordBonus);

        // Update human hand: remove attempted letters, add back wrong ones
        StringBuilder newHand = new StringBuilder(human.getHandLetters());
        for (CrosswordLetterRequest letter : letters) {
            char c = Character.toUpperCase(letter.getC());
            int idx = newHand.indexOf(String.valueOf(c));
            if (idx >= 0) newHand.deleteCharAt(idx);
        }
        for (TurnCellResult result : humanResults) {
            if (!result.isCorrect()) newHand.append(result.getLetter());
        }
        human.setHandLetters(newHand.toString());

        // ── 2. Bot turn ────────────────────────────────────────────────────
        int botCount = weightedBotCount();
        List<int[]> botCells = pickCoveredCells(state, botCount);
        List<TurnCellResult> botPlacements = new ArrayList<>();

        for (int[] cell : botCells) {
            char sol = state.solutionAt(cell[0], cell[1]);
            state.setCurrentAt(cell[0], cell[1], sol);
            bot.setPoints(bot.getPoints() + 1);
            botPlacements.add(TurnCellResult.builder()
                    .row(cell[0]).column(cell[1]).letter(sol).correct(true).build());
        }

        Set<UUID> completeAfterBot = completeWords(state);
        int botWordBonus = newlyCompleted(state, completeAfterHuman, completeAfterBot)
                .stream().mapToInt(CompletedWordResult::getPoints).sum();
        bot.setPoints(bot.getPoints() + botWordBonus);

        // ── 3. Drop any human letters that have no remaining uncovered cell,
        //        then refill both hands from the same pool to avoid overlap ──
        dropOrphanedLetters(state, human);
        refillBothHands(state, human, bot, 5);

        TurnResultResponse turnResult = TurnResultResponse.builder()
                .humanResults(humanResults)
                .humanWordBonus(humanWordBonus)
                .humanCompletedWords(humanCompletedWords)
                .botPlacements(botPlacements)
                .botWordBonus(botWordBonus)
                .build();

        return new TurnOutcome(state, turnResult);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private CrosswordPlayer findHuman(CrosswordState state) {
        return state.getPlayers().stream().filter(p -> !p.isBot()).findFirst().orElseThrow();
    }

    private CrosswordPlayer findBot(CrosswordState state) {
        return state.getPlayers().stream().filter(CrosswordPlayer::isBot).findFirst().orElseThrow();
    }

    private Set<UUID> completeWords(CrosswordState state) {
        Set<UUID> complete = new HashSet<>();
        for (CrosswordStateTerm term : state.getTerms()) {
            if (isWordComplete(state, term)) complete.add(term.getId());
        }
        return complete;
    }

    private boolean isWordComplete(CrosswordState state, CrosswordStateTerm term) {
        int r = term.getRow(), c = term.getColumn();
        String word = term.getCrosswordTerm().getTerm().toUpperCase();
        int dr = term.getDirection() == Direction.DOWN ? 1 : 0;
        int dc = term.getDirection() == Direction.ACROSS ? 1 : 0;
        for (int i = 0; i < word.length(); i++) {
            if (state.currentAt(r + dr * i, c + dc * i) == CrosswordState.UNCOVERED_FIELD) return false;
        }
        return true;
    }

    private List<CompletedWordResult> newlyCompleted(CrosswordState state, Set<UUID> before, Set<UUID> after) {
        List<CompletedWordResult> results = new ArrayList<>();
        for (CrosswordStateTerm term : state.getTerms()) {
            if (!before.contains(term.getId()) && after.contains(term.getId())) {
                String word = term.getCrosswordTerm().getTerm();
                results.add(CompletedWordResult.builder()
                        .word(word)
                        .points(word.length())
                        .build());
            }
        }
        return results;
    }

    private int weightedBotCount() {
        int r = RANDOM.nextInt(100);
        if (r < 10) return 1;
        if (r < 35) return 2;
        if (r < 70) return 3;
        if (r < 90) return 4;
        return 5;
    }

    private List<int[]> pickCoveredCells(CrosswordState state, int count) {
        List<int[]> covered = new ArrayList<>();
        for (int r = 0; r < state.getHeight(); r++) {
            for (int c = 0; c < state.getWidth(); c++) {
                if (state.currentAt(r, c) == CrosswordState.UNCOVERED_FIELD) {
                    covered.add(new int[]{r, c});
                }
            }
        }
        Collections.shuffle(covered);
        return covered.subList(0, Math.min(count, covered.size()));
    }

    private void dropOrphanedLetters(CrosswordState state, CrosswordPlayer player) {
        Set<Character> available = new HashSet<>();
        for (int r = 0; r < state.getHeight(); r++) {
            for (int c = 0; c < state.getWidth(); c++) {
                if (state.currentAt(r, c) == CrosswordState.UNCOVERED_FIELD) {
                    available.add(state.solutionAt(r, c));
                }
            }
        }
        String filtered = player.getHandLetters().chars()
                .filter(c -> available.contains((char) c))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        player.setHandLetters(filtered);
    }

    private void refillBothHands(CrosswordState state, CrosswordPlayer human, CrosswordPlayer bot, int targetSize) {
        int humanNeeded = Math.max(0, targetSize - human.getHandLetters().length());
        int botNeeded = Math.max(0, targetSize - bot.getHandLetters().length());
        List<int[]> pool = pickCoveredCells(state, humanNeeded + botNeeded);

        StringBuilder humanHand = new StringBuilder(human.getHandLetters());
        for (int i = 0; i < Math.min(humanNeeded, pool.size()); i++) {
            humanHand.append(state.solutionAt(pool.get(i)[0], pool.get(i)[1]));
        }
        human.setHandLetters(humanHand.toString());

        StringBuilder botHand = new StringBuilder(bot.getHandLetters());
        for (int i = humanNeeded; i < pool.size(); i++) {
            botHand.append(state.solutionAt(pool.get(i)[0], pool.get(i)[1]));
        }
        bot.setHandLetters(botHand.toString());
    }

}
