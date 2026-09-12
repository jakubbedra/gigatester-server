package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordLetterRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.SubmitWordRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CompletedWordResult;
import com.konfyrm.gigatester.crosswords.domain.dto.response.TurnCellResult;
import com.konfyrm.gigatester.crosswords.domain.dto.response.TurnResultResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.WordTurnResultResponse;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordPlayer;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordStateTerm;
import com.konfyrm.gigatester.crosswords.domain.entity.enums.BotDifficulty;
import com.konfyrm.gigatester.crosswords.domain.entity.enums.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class CrosswordTurnService {

    private static final Random RANDOM = new Random();

    public record TurnOutcome(CrosswordState state, TurnResultResponse result) {}

    public record WordTurnOutcome(CrosswordState state, WordTurnResultResponse result) {}

    /**
     * WORDS mode: the player names a whole word for one clue. A correct answer fills that
     * word into the grid and scores exactly its length; a wrong answer scores nothing (never
     * negative). The bot then gets its own turn at a random unsolved word, with per-difficulty
     * odds of actually solving it, mirroring the letters-per-turn scaling used in LETTERS mode.
     */
    public WordTurnOutcome processWordTurn(CrosswordState state, SubmitWordRequest request) {
        CrosswordPlayer human = findHuman(state);
        CrosswordPlayer bot = findBot(state);

        CrosswordStateTerm term = state.getTerms().stream()
                .filter(t -> t.getId().equals(request.getTermId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found in this crossword"));
        if (term.isSolved()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That word has already been solved");
        }

        String answer = normalizeWord(term.getCrosswordTerm().getTerm());
        String guess = normalizeWord(request.getWord());
        boolean correct = !answer.isEmpty() && answer.equals(guess);

        String humanWord = null;
        int humanPoints = 0;
        if (correct) {
            fillWord(state, term);
            term.setSolved(true);
            humanPoints = answer.length();
            human.setPoints(human.getPoints() + humanPoints);
            humanWord = answer;
        }

        // Bot's turn: it always attempts a random still-unsolved word, but — like a human
        // guessing — only actually gets it right some of the time, per difficulty. A miss
        // is a real, visible whiff (botAttempted && !botCorrect), not a silently skipped turn.
        List<CrosswordStateTerm> unsolved = state.getTerms().stream().filter(t -> !t.isSolved()).toList();
        boolean botAttempted = !unsolved.isEmpty();
        boolean botCorrect = botAttempted && RANDOM.nextInt(100) < botWordSolveChance(state.getBotDifficulty());
        String botWord = null;
        int botPoints = 0;
        if (botCorrect) {
            CrosswordStateTerm botTerm = unsolved.get(RANDOM.nextInt(unsolved.size()));
            fillWord(state, botTerm);
            botTerm.setSolved(true);
            botWord = CrosswordTextUtils.toGridCase(botTerm.getCrosswordTerm().getTerm());
            botPoints = botWord.length();
            bot.setPoints(bot.getPoints() + botPoints);
        }

        WordTurnResultResponse result = WordTurnResultResponse.builder()
                .humanCorrect(correct)
                .humanWord(humanWord)
                .humanPoints(humanPoints)
                .botAttempted(botAttempted)
                .botCorrect(botCorrect)
                .botWord(botWord)
                .botPoints(botPoints)
                .build();

        return new WordTurnOutcome(state, result);
    }

    private int botWordSolveChance(BotDifficulty difficulty) {
        BotDifficulty effective = difficulty != null ? difficulty : BotDifficulty.NORMAL;
        return switch (effective) {
            case EASY -> 20;
            case NORMAL -> 70;
            case HARD -> 85;
            case IMPOSSIBLE -> 100;
        };
    }

    private void fillWord(CrosswordState state, CrosswordStateTerm term) {
        String word = CrosswordTextUtils.toGridCase(term.getCrosswordTerm().getTerm());
        int r = term.getRow(), c = term.getColumn();
        int dr = term.getDirection() == Direction.DOWN ? 1 : 0;
        int dc = term.getDirection() == Direction.ACROSS ? 1 : 0;
        for (int i = 0; i < word.length(); i++) {
            int rr = r + dr * i, cc = c + dc * i;
            if (state.currentAt(rr, cc) == CrosswordState.UNCOVERED_FIELD) {
                state.setCurrentAt(rr, cc, state.solutionAt(rr, cc));
            }
        }
    }

    /** Grid-case, trimmed, and internal whitespace runs collapsed to one space, for forgiving word matching. */
    private String normalizeWord(String raw) {
        if (raw == null) return "";
        return CrosswordTextUtils.toGridCase(raw.trim().replaceAll("\\s+", " "));
    }

    public TurnOutcome processTurn(CrosswordState state, List<CrosswordLetterRequest> letters) {
        CrosswordPlayer human = findHuman(state);
        CrosswordPlayer bot = findBot(state);

        // ── 1. Validate human letters ──────────────────────────────────────
        List<TurnCellResult> humanResults = new ArrayList<>();

        Set<UUID> completeBefore = completeWords(state);

        for (CrosswordLetterRequest letter : letters) {
            int r = letter.getRow(), c = letter.getColumn();
            char played = CrosswordTextUtils.toGridCase(letter.getC());
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
            char c = CrosswordTextUtils.toGridCase(letter.getC());
            int idx = newHand.indexOf(String.valueOf(c));
            if (idx >= 0) newHand.deleteCharAt(idx);
        }
        for (TurnCellResult result : humanResults) {
            if (!result.isCorrect()) newHand.append(result.getLetter());
        }
        human.setHandLetters(newHand.toString());

        // ── 2. Bot turn ────────────────────────────────────────────────────
        int botCount = botCount(state.getBotDifficulty());
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
        String word = CrosswordTextUtils.toGridCase(term.getCrosswordTerm().getTerm());
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

    private int botCount(BotDifficulty difficulty) {
        BotDifficulty effective = difficulty != null ? difficulty : BotDifficulty.NORMAL;
        return switch (effective) {
            case EASY -> RANDOM.nextInt(3);            // 0-2, at most 2
            case NORMAL -> weightedBotCount();          // 1-5, weighted toward the middle
            case HARD -> 3 + RANDOM.nextInt(3);          // 3-5
            case IMPOSSIBLE -> 5;                        // always 5
        };
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
        Map<Character, Integer> available = new HashMap<>();
        for (int r = 0; r < state.getHeight(); r++) {
            for (int c = 0; c < state.getWidth(); c++) {
                if (state.currentAt(r, c) == CrosswordState.UNCOVERED_FIELD) {
                    available.merge(state.solutionAt(r, c), 1, Integer::sum);
                }
            }
        }
        Map<Character, Integer> remaining = new HashMap<>(available);
        StringBuilder filtered = new StringBuilder();
        for (char ch : player.getHandLetters().toCharArray()) {
            int count = remaining.getOrDefault(ch, 0);
            if (count > 0) {
                filtered.append(ch);
                remaining.put(ch, count - 1);
            }
        }
        player.setHandLetters(filtered.toString());
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
