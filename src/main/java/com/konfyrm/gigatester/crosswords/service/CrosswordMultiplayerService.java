package com.konfyrm.gigatester.crosswords.service;

import com.konfyrm.gigatester.crosswords.domain.dto.enums.DirectionDto;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordLetterRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.request.CrosswordStateRequest;
import com.konfyrm.gigatester.crosswords.domain.dto.response.CrosswordStateClueResponse;
import com.konfyrm.gigatester.crosswords.domain.entity.CrosswordState;
import com.konfyrm.gigatester.crosswords.domain.dto.response.MultiplayerSessionResponse;
import com.konfyrm.gigatester.crosswords.domain.dto.response.MultiplayerSessionSummaryResponse;
import com.konfyrm.gigatester.crosswords.domain.entity.*;
import com.konfyrm.gigatester.crosswords.domain.entity.enums.Direction;
import com.konfyrm.gigatester.crosswords.repository.CrosswordMultiplayerSessionRepository;
import com.konfyrm.gigatester.crosswords.repository.CrosswordRepository;
import com.konfyrm.gigatester.subjects.service.SubjectGroupAccessService;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrosswordMultiplayerService {

    private final CrosswordMultiplayerSessionRepository sessionRepository;
    private final CrosswordRepository crosswordRepository;
    private final CrosswordGeneratorService generatorService;
    private final SubjectGroupAccessService accessService;

    @Autowired
    public CrosswordMultiplayerService(
            CrosswordMultiplayerSessionRepository sessionRepository,
            CrosswordRepository crosswordRepository,
            CrosswordGeneratorService generatorService,
            SubjectGroupAccessService accessService
    ) {
        this.sessionRepository = sessionRepository;
        this.crosswordRepository = crosswordRepository;
        this.generatorService = generatorService;
        this.accessService = accessService;
    }

    @Transactional
    public MultiplayerSessionResponse createSession(CrosswordStateRequest request, User player1) {
        if (!accessService.hasAccessToCrossword(request.getCrosswordId(), player1)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        Crossword crossword = crosswordRepository.findById(request.getCrosswordId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crossword not found"));

        List<CrosswordTerm> filteredTerms = CrosswordTagFilter.filter(crossword.getTerms(), request.getTagFilter(), request.getTagFilterMode(), request.isMatchAllTags());

        CrosswordState generated = generatorService.generate(crossword, filteredTerms, request.getNumberOfWords());

        List<CrosswordMultiplayerTerm> terms = generated.getTerms().stream()
                .map(t -> CrosswordMultiplayerTerm.builder()
                        .crosswordTerm(t.getCrosswordTerm())
                        .row(t.getRow())
                        .column(t.getColumn())
                        .direction(t.getDirection())
                        .build())
                .collect(Collectors.toList());

        String player1Hand = drawHandFromGrid(generated, 5, new ArrayList<>());

        CrosswordMultiplayerSession session = CrosswordMultiplayerSession.builder()
                .crossword(crossword)
                .player1(player1)
                .player2(null)
                .status(CrosswordMultiplayerStatus.WAITING_FOR_PLAYER)
                .currentTurnUserId(player1.getId())
                .solutionGrid(generated.getSolutionGrid())
                .currentGrid(generated.getCurrentGrid())
                .width(generated.getWidth())
                .height(generated.getHeight())
                .player1Hand(player1Hand)
                .player1Points(0)
                .player2Hand("")
                .player2Points(0)
                .terms(terms)
                .createdAt(LocalDateTime.now())
                .build();

        session = sessionRepository.save(session);
        return toResponse(session, player1.getId());
    }

    @Transactional
    public MultiplayerSessionResponse joinSession(UUID sessionId, User player2) {
        CrosswordMultiplayerSession session = findSession(sessionId);

        if (session.getStatus() != CrosswordMultiplayerStatus.WAITING_FOR_PLAYER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session is not open for joining");
        }
        if (session.getPlayer1().getId().equals(player2.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot join your own session");
        }
        if (!accessService.hasAccessToCrossword(session.getCrossword().getId(), player2)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this crossword");
        }

        List<Character> alreadyDealt = new ArrayList<>(handAsList(session.getPlayer1Hand()));
        String player2Hand = drawHandFromGrid(session, 5, alreadyDealt);

        session.setPlayer2(player2);
        session.setPlayer2Hand(player2Hand);
        session.setStatus(CrosswordMultiplayerStatus.IN_PROGRESS);

        session = sessionRepository.save(session);
        return toResponse(session, player2.getId());
    }

    @Transactional
    public MultiplayerSessionResponse submitTurn(UUID sessionId, List<CrosswordLetterRequest> letters, User user) {
        CrosswordMultiplayerSession session = findSession(sessionId);

        if (session.getStatus() != CrosswordMultiplayerStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Session is not in progress");
        }
        if (!session.getCurrentTurnUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not your turn");
        }

        boolean isPlayer1 = session.getPlayer1().getId().equals(user.getId());
        String currentHand = isPlayer1 ? session.getPlayer1Hand() : session.getPlayer2Hand();
        int currentPoints = isPlayer1 ? session.getPlayer1Points() : session.getPlayer2Points();

        StringBuilder hand = new StringBuilder(currentHand);
        int pointsDelta = 0;
        List<Character> wrongLetters = new ArrayList<>();

        Set<UUID> completeBefore = completeTermIds(session);

        for (CrosswordLetterRequest letter : letters) {
            int r = letter.getRow(), c = letter.getColumn();
            char played = Character.toUpperCase(letter.getC());
            boolean correct = session.currentAt(r, c) == CrosswordState.UNCOVERED_FIELD
                    && session.solutionAt(r, c) == played;

            // Remove from hand regardless; wrong letters are re-added below
            int idx = hand.indexOf(String.valueOf(played));
            if (idx >= 0) hand.deleteCharAt(idx);

            if (correct) {
                session.setCurrentAt(r, c, played);
                pointsDelta++;
            } else {
                pointsDelta = Math.max(0, pointsDelta - 1);
                wrongLetters.add(played);
            }
        }
        // Return wrong letters to hand
        for (char c : wrongLetters) hand.append(c);

        // Word-completion bonus
        Set<UUID> completeAfter = completeTermIds(session);
        int wordBonus = 0;
        for (UUID termId : completeAfter) {
            if (!completeBefore.contains(termId)) {
                wordBonus += session.getTerms().stream()
                        .filter(t -> t.getId().equals(termId))
                        .mapToInt(t -> t.getCrosswordTerm().getTerm().length())
                        .sum();
            }
        }

        currentPoints = currentPoints + pointsDelta + wordBonus;

        // Refill hand from uncovered grid cells
        String refilled = refillHand(session, hand.toString(), 5, isPlayer1);

        if (isPlayer1) {
            session.setPlayer1Hand(refilled);
            session.setPlayer1Points(currentPoints);
        } else {
            session.setPlayer2Hand(refilled);
            session.setPlayer2Points(currentPoints);
        }

        // Switch turn
        UUID opponentId = isPlayer1 ? session.getPlayer2().getId() : session.getPlayer1().getId();
        session.setCurrentTurnUserId(opponentId);

        // Check completion
        if (!session.getCurrentGrid().contains(String.valueOf(CrosswordState.UNCOVERED_FIELD))) {
            session.setStatus(CrosswordMultiplayerStatus.FINISHED);
        }

        session = sessionRepository.save(session);
        return toResponse(session, user.getId());
    }

    public MultiplayerSessionResponse getSession(UUID sessionId, User user) {
        CrosswordMultiplayerSession session = findSession(sessionId);
        if (!session.getPlayer1().getId().equals(user.getId())
                && (session.getPlayer2() == null || !session.getPlayer2().getId().equals(user.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant");
        }
        return toResponse(session, user.getId());
    }

    public List<MultiplayerSessionSummaryResponse> getMySessions(User user) {
        return sessionRepository.findByUserId(user.getId()).stream()
                .filter(s -> s.getStatus() != CrosswordMultiplayerStatus.ABANDONED)
                .map(s -> toSummary(s, user.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void abandonSession(UUID sessionId, User user) {
        CrosswordMultiplayerSession session = findSession(sessionId);
        if (!session.getPlayer1().getId().equals(user.getId())
                && (session.getPlayer2() == null || !session.getPlayer2().getId().equals(user.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant");
        }
        session.setStatus(CrosswordMultiplayerStatus.ABANDONED);
        sessionRepository.save(session);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CrosswordMultiplayerSession findSession(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
    }

    private Set<UUID> completeTermIds(CrosswordMultiplayerSession session) {
        Set<UUID> complete = new HashSet<>();
        for (CrosswordMultiplayerTerm term : session.getTerms()) {
            String word = term.getCrosswordTerm().getTerm().toUpperCase();
            int r = term.getRow(), c = term.getColumn();
            int dr = term.getDirection() == Direction.DOWN ? 1 : 0;
            int dc = term.getDirection() == Direction.ACROSS ? 1 : 0;
            boolean done = true;
            for (int i = 0; i < word.length(); i++) {
                if (session.currentAt(r + dr * i, c + dc * i) == CrosswordState.UNCOVERED_FIELD) {
                    done = false;
                    break;
                }
            }
            if (done) complete.add(term.getId());
        }
        return complete;
    }

    private String drawHandFromGrid(CrosswordState state, int count, List<Character> exclude) {
        List<Character> pool = new ArrayList<>();
        for (int r = 0; r < state.getHeight(); r++) {
            for (int c = 0; c < state.getWidth(); c++) {
                if (state.currentAt(r, c) == CrosswordState.UNCOVERED_FIELD) {
                    pool.add(state.solutionAt(r, c));
                }
            }
        }
        List<Character> mutableExclude = new ArrayList<>(exclude);
        pool.removeIf(ch -> {
            int idx = mutableExclude.indexOf(ch);
            if (idx >= 0) { mutableExclude.remove(idx); return true; }
            return false;
        });
        Collections.shuffle(pool);
        StringBuilder hand = new StringBuilder();
        for (int i = 0; i < Math.min(count, pool.size()); i++) hand.append(pool.get(i));
        return hand.toString();
    }

    private String drawHandFromGrid(CrosswordMultiplayerSession session, int count, List<Character> exclude) {
        List<Character> pool = new ArrayList<>();
        for (int r = 0; r < session.getHeight(); r++) {
            for (int c = 0; c < session.getWidth(); c++) {
                if (session.currentAt(r, c) == CrosswordState.UNCOVERED_FIELD) {
                    pool.add(session.solutionAt(r, c));
                }
            }
        }
        List<Character> mutableExclude = new ArrayList<>(exclude);
        pool.removeIf(ch -> {
            int idx = mutableExclude.indexOf(ch);
            if (idx >= 0) { mutableExclude.remove(idx); return true; }
            return false;
        });
        Collections.shuffle(pool);
        StringBuilder hand = new StringBuilder();
        for (int i = 0; i < Math.min(count, pool.size()); i++) hand.append(pool.get(i));
        return hand.toString();
    }

    private String refillHand(CrosswordMultiplayerSession session, String currentHand, int targetSize, boolean isPlayer1) {
        if (currentHand.length() >= targetSize) return currentHand;
        int needed = targetSize - currentHand.length();
        // Exclude letters already in this player's updated hand + opponent's current hand
        // (do NOT read session.getPlayerXHand() — those still hold stale pre-turn values)
        String opponentHand = isPlayer1 ? session.getPlayer2Hand() : session.getPlayer1Hand();
        List<Character> exclude = new ArrayList<>(handAsList(currentHand));
        exclude.addAll(handAsList(opponentHand));
        String added = drawHandFromGrid(session, needed, exclude);
        return currentHand + added;
    }

    private List<Character> handAsList(String hand) {
        List<Character> list = new ArrayList<>();
        if (hand == null) return list;
        for (char c : hand.toCharArray()) list.add(c);
        return list;
    }

    public MultiplayerSessionResponse toResponse(CrosswordMultiplayerSession session, UUID requestingUserId) {
        boolean isPlayer1 = session.getPlayer1().getId().equals(requestingUserId);
        String myHand = isPlayer1 ? session.getPlayer1Hand() : session.getPlayer2Hand();
        boolean myTurn = requestingUserId.equals(session.getCurrentTurnUserId());

        return MultiplayerSessionResponse.builder()
                .id(session.getId())
                .crosswordId(session.getCrossword().getId())
                .crosswordName(session.getCrossword().getName())
                .player1(toPlayerInfo(session.getPlayer1()))
                .player2(session.getPlayer2() != null ? toPlayerInfo(session.getPlayer2()) : null)
                .status(session.getStatus().name())
                .currentTurnUserId(session.getCurrentTurnUserId())
                .myTurn(myTurn)
                .currentGrid(session.getCurrentGrid())
                .width(session.getWidth())
                .height(session.getHeight())
                .myHand(myHand)
                .player1Points(session.getPlayer1Points())
                .player2Points(session.getPlayer2Points())
                .clues(toClueResponses(session.getTerms()))
                .createdAt(session.getCreatedAt())
                .build();
    }

    private MultiplayerSessionSummaryResponse toSummary(CrosswordMultiplayerSession session, UUID userId) {
        boolean isPlayer1 = session.getPlayer1().getId().equals(userId);
        User opponent = isPlayer1 ? session.getPlayer2() : session.getPlayer1();
        int myScore = isPlayer1 ? session.getPlayer1Points() : session.getPlayer2Points();
        int opScore = isPlayer1 ? session.getPlayer2Points() : session.getPlayer1Points();
        boolean myTurn = userId.equals(session.getCurrentTurnUserId());

        return MultiplayerSessionSummaryResponse.builder()
                .id(session.getId())
                .crosswordId(session.getCrossword().getId())
                .crosswordName(session.getCrossword().getName())
                .opponent(opponent != null ? toPlayerInfo(opponent) : null)
                .status(session.getStatus().name())
                .myTurn(myTurn)
                .myScore(myScore)
                .opponentScore(opScore)
                .createdAt(session.getCreatedAt())
                .build();
    }

    private MultiplayerSessionResponse.PlayerInfo toPlayerInfo(User user) {
        return MultiplayerSessionResponse.PlayerInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    private List<CrosswordStateClueResponse> toClueResponses(List<CrosswordMultiplayerTerm> terms) {
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
