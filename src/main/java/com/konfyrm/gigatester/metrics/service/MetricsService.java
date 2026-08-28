package com.konfyrm.gigatester.metrics.service;

import com.konfyrm.gigatester.metrics.domain.dto.*;
import com.konfyrm.gigatester.metrics.domain.entity.UserQuestionStat;
import com.konfyrm.gigatester.metrics.domain.entity.UserTestStat;
import com.konfyrm.gigatester.metrics.repository.DailyStreakRepository;
import com.konfyrm.gigatester.metrics.repository.UserQuestionStatRepository;
import com.konfyrm.gigatester.metrics.repository.UserTestStatRepository;
import com.konfyrm.gigatester.tags.entity.Tag;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.domain.entity.Test;
import com.konfyrm.gigatester.tests.domain.entity.TestMode;
import com.konfyrm.gigatester.tests.domain.entity.TestState;
import com.konfyrm.gigatester.tests.repository.TestRepository;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private final UserTestStatRepository testStatRepository;
    private final UserQuestionStatRepository questionStatRepository;
    private final DailyStreakRepository dailyStreakRepository;
    private final TestRepository testRepository;

    @Autowired
    public MetricsService(
            UserTestStatRepository testStatRepository,
            UserQuestionStatRepository questionStatRepository,
            DailyStreakRepository dailyStreakRepository,
            TestRepository testRepository
    ) {
        this.testStatRepository = testStatRepository;
        this.questionStatRepository = questionStatRepository;
        this.dailyStreakRepository = dailyStreakRepository;
        this.testRepository = testRepository;
    }

    /** EXAM mode: single pass, so the live question list at completion is the whole attempt. */
    @Transactional
    public void recordTestCompletion(User user, TestState testState) {
        List<QuestionState> questions = testState.getQuestions();
        int total = questions.size();
        int correct = (int) questions.stream().filter(QuestionState::isWasCorrectAnswer).count();
        saveTestStat(user, testState, total, correct);
        recordQuestionAttempts(user, questions);
    }

    /**
     * LEARNING mode: rounds discard superseded QuestionStates as they're retried,
     * so the caller (TestStateService) accumulates total/correct across every round
     * itself and already called recordQuestionAttempts for each one — this only
     * needs to save the summary row, not repeat the per-question recording.
     */
    @Transactional
    public void recordTestCompletion(User user, TestState testState, int total, int correct) {
        saveTestStat(user, testState, total, correct);
    }

    private void saveTestStat(User user, TestState testState, int total, int correct) {
        double scorePercent = total > 0 ? (double) correct / total * 100.0 : 0.0;
        boolean passed = testState.getPassingPercentage() == null || scorePercent >= testState.getPassingPercentage();

        testStatRepository.save(UserTestStat.builder()
                .user(user)
                .test(testState.getTest())
                .passed(passed)
                .scorePercent(scorePercent)
                .totalQuestions(total)
                .correctQuestions(correct)
                .completedDate(LocalDate.now())
                .completedAt(LocalDateTime.now())
                .mode(testState.getMode())
                .build());
    }

    /** Upserts UserQuestionStat for every answered question in the given batch — call once per round in LEARNING mode. */
    @Transactional
    public void recordQuestionAttempts(User user, List<QuestionState> questions) {
        for (QuestionState qs : questions) {
            if (!qs.isAnswered()) continue;
            UserQuestionStat stat = questionStatRepository
                    .findByUser_IdAndQuestion_Id(user.getId(), qs.getQuestion().getId())
                    .orElseGet(() -> UserQuestionStat.builder()
                            .user(user)
                            .question(qs.getQuestion())
                            .timesAnswered(0)
                            .timesCorrect(0)
                            .build());
            stat.setTimesAnswered(stat.getTimesAnswered() + 1);
            if (qs.isWasCorrectAnswer()) stat.setTimesCorrect(stat.getTimesCorrect() + 1);
            questionStatRepository.save(stat);
        }
    }

    public ProgressResponse getProgress(User user, UUID testId, TestMode mode, LocalDate from, LocalDate to) {
        List<UserTestStat> stats = testId != null
                ? testStatRepository.findByUser_IdAndTest_Id(user.getId(), testId)
                : testStatRepository.findByUser_Id(user.getId());
        if (mode != null) {
            stats = stats.stream().filter(s -> s.getMode() == mode).collect(Collectors.toList());
        }
        stats = filterByDateRange(stats, from, to);

        int totalTestsTaken = stats.size();
        int totalTestsPassed = (int) stats.stream().filter(UserTestStat::isPassed).count();
        int totalQuestionsAnswered = stats.stream().mapToInt(UserTestStat::getTotalQuestions).sum();
        int totalQuestionsCorrect = stats.stream().mapToInt(UserTestStat::getCorrectQuestions).sum();

        List<DailyStatDto> dailyStats = stats.stream()
                .collect(Collectors.groupingBy(UserTestStat::getCompletedDate))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> DailyStatDto.builder()
                        .date(e.getKey())
                        .testsTaken(e.getValue().size())
                        .testsPassed((int) e.getValue().stream().filter(UserTestStat::isPassed).count())
                        .questionsAnswered(e.getValue().stream().mapToInt(UserTestStat::getTotalQuestions).sum())
                        .questionsCorrect(e.getValue().stream().mapToInt(UserTestStat::getCorrectQuestions).sum())
                        .build())
                .collect(Collectors.toList());

        // Distinct tests taken by this user for the filter dropdown
        List<TestSummaryForMetricsDto> myTests = testStatRepository.findByUser_Id(user.getId()).stream()
                .map(UserTestStat::getTest)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Test::getId, t -> t, (a, b) -> a))
                .values().stream()
                .map(t -> TestSummaryForMetricsDto.builder().id(t.getId()).name(t.getName()).build())
                .sorted(Comparator.comparing(TestSummaryForMetricsDto::getName))
                .collect(Collectors.toList());

        List<TagAccuracyDto> tagStats = testId != null ? getTagStats(user, testId) : List.of();

        return ProgressResponse.builder()
                .totalTestsTaken(totalTestsTaken)
                .totalTestsPassed(totalTestsPassed)
                .totalQuestionsAnswered(totalQuestionsAnswered)
                .totalQuestionsCorrect(totalQuestionsCorrect)
                .dailyStats(dailyStats)
                .myTests(myTests)
                .tagStats(tagStats)
                .build();
    }

    private List<UserTestStat> filterByDateRange(List<UserTestStat> stats, LocalDate from, LocalDate to) {
        if (from == null && to == null) return stats;
        return stats.stream()
                .filter(s -> (from == null || !s.getCompletedDate().isBefore(from))
                        && (to == null || !s.getCompletedDate().isAfter(to)))
                .collect(Collectors.toList());
    }

    public List<TagAccuracyDto> getTagStats(User user, UUID testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<UUID> questionIds = test.getQuestions().stream()
                .map(q -> q.getId())
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) return List.of();

        List<UserQuestionStat> qStats = questionStatRepository
                .findByUser_IdAndQuestion_IdIn(user.getId(), questionIds);

        // Group accuracy by tag
        Map<UUID, int[]> tagAcc = new LinkedHashMap<>(); // [timesAnswered, timesCorrect]
        Map<UUID, String> tagKeys = new LinkedHashMap<>();
        for (UserQuestionStat qs : qStats) {
            for (Tag tag : qs.getQuestion().getTags()) {
                tagAcc.computeIfAbsent(tag.getId(), k -> new int[2]);
                tagKeys.put(tag.getId(), tag.getKey());
                tagAcc.get(tag.getId())[0] += qs.getTimesAnswered();
                tagAcc.get(tag.getId())[1] += qs.getTimesCorrect();
            }
        }

        return tagAcc.entrySet().stream()
                .map(e -> {
                    int answered = e.getValue()[0];
                    int correct = e.getValue()[1];
                    return TagAccuracyDto.builder()
                            .tagId(e.getKey())
                            .tagKey(tagKeys.get(e.getKey()))
                            .timesAnswered(answered)
                            .timesCorrect(correct)
                            .accuracy(answered > 0 ? (double) correct / answered * 100.0 : 0.0)
                            .build();
                })
                .sorted(Comparator.comparingDouble(TagAccuracyDto::getAccuracy))
                .collect(Collectors.toList());
    }

    public List<RankingEntryDto> getRanking(UUID testId, String sortBy, TestMode mode, LocalDate from, LocalDate to) {
        List<UserTestStat> allStats = testId != null
                ? testStatRepository.findByTest_Id(testId)
                : testStatRepository.findAll();
        if (mode != null) {
            allStats = allStats.stream().filter(s -> s.getMode() == mode).collect(Collectors.toList());
        }
        allStats = filterByDateRange(allStats, from, to);

        // Pre-load all streaks keyed by userId
        Map<UUID, Integer> streakMap = dailyStreakRepository.findAll().stream()
                .collect(Collectors.toMap(s -> s.getUser().getId(), s -> s.getCurrentStreak()));

        List<RankingEntryDto> ranking = allStats.stream()
                .collect(Collectors.groupingBy(s -> s.getUser().getId()))
                .values().stream()
                .map(userStats -> {
                    User u = userStats.get(0).getUser();
                    int taken = userStats.size();
                    int passed = (int) userStats.stream().filter(UserTestStat::isPassed).count();
                    int correct = userStats.stream().mapToInt(UserTestStat::getCorrectQuestions).sum();
                    double passRate = taken > 0 ? (double) passed / taken * 100.0 : 0.0;
                    int streak = streakMap.getOrDefault(u.getId(), 0);
                    return RankingEntryDto.builder()
                            .userId(u.getId().toString())
                            .username(u.getUsername())
                            .totalTestsTaken(taken)
                            .totalTestsPassed(passed)
                            .passRate(passRate)
                            .totalQuestionsCorrect(correct)
                            .currentStreak(streak)
                            .build();
                })
                .sorted(comparatorFor(sortBy))
                .collect(Collectors.toList());

        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setRank(i + 1);
        }
        return ranking;
    }

    private Comparator<RankingEntryDto> comparatorFor(String sortBy) {
        return switch (sortBy == null ? "accuracy" : sortBy) {
            case "streak" -> Comparator.comparingInt(RankingEntryDto::getCurrentStreak).reversed();
            case "points" -> Comparator.comparingInt(RankingEntryDto::getTotalQuestionsCorrect).reversed();
            default -> Comparator.comparingDouble(RankingEntryDto::getPassRate).reversed()
                    .thenComparing(Comparator.comparingInt(RankingEntryDto::getTotalQuestionsCorrect).reversed());
        };
    }

    /**
     * Reorders questions so that those the user gets wrong most often appear first.
     * Uses weighted reservoir sampling: score = -ln(U) / w, where w ∈ [0.1, 1.0].
     * A high error rate → high w → high expected score → appears earlier.
     */
    public void applyWeightedShuffle(TestState state, User user) {
        List<QuestionState> questions = new ArrayList<>(state.getQuestions());
        if (questions.isEmpty()) return;

        List<UUID> questionIds = questions.stream().map(qs -> qs.getQuestion().getId()).toList();
        Map<UUID, UserQuestionStat> statMap = questionStatRepository
                .findByUser_IdAndQuestion_IdIn(user.getId(), questionIds)
                .stream()
                .collect(Collectors.toMap(s -> s.getQuestion().getId(), s -> s));

        Random random = new Random();
        questions.sort((a, b) -> Double.compare(
                weightedScore(statMap.get(b.getQuestion().getId()), random),
                weightedScore(statMap.get(a.getQuestion().getId()), random)
        ));

        state.getQuestions().clear();
        state.getQuestions().addAll(questions);
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setSequence(i);
        }
    }

    private double weightedScore(UserQuestionStat stat, Random random) {
        double u = Math.max(random.nextDouble(), 1e-9);
        double weight;
        if (stat == null || stat.getTimesAnswered() == 0) {
            weight = 0.5;
        } else {
            double errorRate = (double)(stat.getTimesAnswered() - stat.getTimesCorrect()) / stat.getTimesAnswered();
            weight = 0.1 + errorRate * 0.9;
        }
        return -Math.log(u) / weight;
    }
}
