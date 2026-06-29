package com.konfyrm.gigatester.metrics.service;

import com.konfyrm.gigatester.metrics.domain.dto.DailyStatDto;
import com.konfyrm.gigatester.metrics.domain.dto.ProgressResponse;
import com.konfyrm.gigatester.metrics.domain.dto.RankingEntryDto;
import com.konfyrm.gigatester.metrics.domain.entity.UserQuestionStat;
import com.konfyrm.gigatester.metrics.domain.entity.UserTestStat;
import com.konfyrm.gigatester.metrics.repository.UserQuestionStatRepository;
import com.konfyrm.gigatester.metrics.repository.UserTestStatRepository;
import com.konfyrm.gigatester.tests.domain.entity.QuestionState;
import com.konfyrm.gigatester.tests.domain.entity.TestState;
import com.konfyrm.gigatester.users.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private final UserTestStatRepository testStatRepository;
    private final UserQuestionStatRepository questionStatRepository;

    @Autowired
    public MetricsService(
            UserTestStatRepository testStatRepository,
            UserQuestionStatRepository questionStatRepository
    ) {
        this.testStatRepository = testStatRepository;
        this.questionStatRepository = questionStatRepository;
    }

    @Transactional
    public void recordTestCompletion(User user, TestState testState) {
        List<QuestionState> questions = testState.getQuestions();
        int total = questions.size();
        int correct = (int) questions.stream().filter(QuestionState::isWasCorrectAnswer).count();
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
                .build());

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

    public ProgressResponse getProgress(User user) {
        List<UserTestStat> stats = testStatRepository.findByUser_Id(user.getId());
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

        return ProgressResponse.builder()
                .totalTestsTaken(totalTestsTaken)
                .totalTestsPassed(totalTestsPassed)
                .totalQuestionsAnswered(totalQuestionsAnswered)
                .totalQuestionsCorrect(totalQuestionsCorrect)
                .dailyStats(dailyStats)
                .build();
    }

    public List<RankingEntryDto> getRanking() {
        List<UserTestStat> allStats = testStatRepository.findAll();
        List<RankingEntryDto> ranking = allStats.stream()
                .collect(Collectors.groupingBy(s -> s.getUser().getId()))
                .values().stream()
                .map(userStats -> {
                    int taken = userStats.size();
                    int passed = (int) userStats.stream().filter(UserTestStat::isPassed).count();
                    int correct = userStats.stream().mapToInt(UserTestStat::getCorrectQuestions).sum();
                    double passRate = taken > 0 ? (double) passed / taken * 100.0 : 0.0;
                    return RankingEntryDto.builder()
                            .username(userStats.get(0).getUser().getUsername())
                            .totalTestsTaken(taken)
                            .totalTestsPassed(passed)
                            .passRate(passRate)
                            .totalQuestionsCorrect(correct)
                            .build();
                })
                .sorted(Comparator.comparingDouble(RankingEntryDto::getPassRate).reversed()
                        .thenComparing(Comparator.comparingInt(RankingEntryDto::getTotalQuestionsCorrect).reversed()))
                .collect(Collectors.toList());

        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setRank(i + 1);
        }
        return ranking;
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
