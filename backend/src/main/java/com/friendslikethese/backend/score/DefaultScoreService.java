package com.friendslikethese.backend.score;

import com.friendslikethese.backend.common.BusinessRuleException;
import com.friendslikethese.backend.common.ResourceNotFoundException;
import com.friendslikethese.backend.event.Event;
import com.friendslikethese.backend.event.EventRepository;
import com.friendslikethese.backend.event.CurrentEventProvider;
import com.friendslikethese.backend.team.Team;
import com.friendslikethese.backend.team.TeamRepository;
import com.friendslikethese.backend.team.TeamResponse;
import com.friendslikethese.backend.round.GameRound;
import com.friendslikethese.backend.round.RoundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DefaultScoreService implements ScoreService {

    private final TeamRepository teamRepository;
    private final ScoreEventRepository scoreEventRepository;
    private final EventRepository eventRepository;
    private final CurrentEventProvider currentEvents;
    private final RoundService rounds;

    public DefaultScoreService(
            TeamRepository teamRepository,
            ScoreEventRepository scoreEventRepository,
            EventRepository eventRepository,
            CurrentEventProvider currentEvents, RoundService rounds
    ) {
        this.teamRepository = teamRepository;
        this.scoreEventRepository = scoreEventRepository;
        this.eventRepository = eventRepository;
        this.currentEvents = currentEvents;
        this.rounds = rounds;
    }

    @Override
    @Transactional
    public TeamResponse adjustScore(UUID teamId, ScoreAdjustmentRequest request) {
        Event current = currentEvents.requireLive();
        GameRound round = rounds.requireActive(current);
        if (request.amount() == 0) {
            throw new BusinessRuleException("Score adjustment cannot be zero.");
        }

        Team team = teamRepository.findByIdAndDeletedFalseForUpdate(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found."));
        if (!team.getEvent().getId().equals(current.getId())) throw new BusinessRuleException("Historical teams are read-only.");

        int previousScore = team.getScore();
        int requestedScore = Math.max(0, previousScore + request.amount());
        int appliedAmount = requestedScore - previousScore;

        ScoreEventType eventType = determineEventType(appliedAmount);

        team.applyScore(requestedScore);

        ScoreEvent scoreEvent = new ScoreEvent(
                team.getEvent(),
                team, round,
                appliedAmount,
                previousScore,
                requestedScore,
                request.reason().trim(),
                eventType
        );

        teamRepository.save(team);
        scoreEventRepository.save(scoreEvent);

        return TeamResponse.from(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScoreEventResponse> getScoreHistory() {
        Event event = findCurrentEvent();

        return scoreEventRepository
                .findTop100ByEventIdOrderByCreatedAtDesc(event.getId())
                .stream()
                .map(ScoreEventResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void resetAllScores() {
        Event event = currentEvents.requireLive();
        GameRound round = rounds.requireActive(event);

        List<Team> teams = teamRepository
                .findByEventIdAndDeletedFalseOrderByScoreDescNameAsc(event.getId());

        for (Team team : teams) {
            int roundScore = scoreEventRepository.findByRoundIdOrderByCreatedAtDesc(round.getId()).stream()
                    .filter(scoreEvent -> scoreEvent.getTeam().getId().equals(team.getId()))
                    .mapToInt(ScoreEvent::getAmount).sum();
            if (roundScore == 0) {
                continue;
            }

            int previousScore = team.getScore();
            int newScore = previousScore - roundScore;
            team.applyScore(Math.max(0, newScore));

            ScoreEvent scoreEvent = new ScoreEvent(
                    event,
                    team, round,
                    -roundScore,
                    previousScore,
                    team.getScore(),
                    "Current round scores reset",
                    ScoreEventType.RESET
            );

            scoreEventRepository.save(scoreEvent);
        }

        teamRepository.saveAll(teams);
    }

    @Override
    @Transactional
    public ScoreActivityResetResponse resetScoreActivity() {
        Event event = currentEvents.requireLive();
        long removed;
        boolean roundBased = true;
        try {
            GameRound round = rounds.requireActive(event);
            removed = scoreEventRepository.deleteByRoundId(round.getId());
        } catch (BusinessRuleException noActiveRound) {
            // Compatibility for pre-round legacy games: their ledger entries are intentionally unassigned.
            removed = scoreEventRepository.deleteByEventId(event.getId());
            roundBased = false;
        }
        if (roundBased) recomputeScores(event);
        return new ScoreActivityResetResponse(removed);
    }

    @Override @Transactional
    public ScoreActivityResetResponse resetEntireGame() {
        Event event = currentEvents.requireLive();
        long removed = scoreEventRepository.deleteByEventId(event.getId());
        teamRepository.findByEventIdOrderByScoreDescNameAsc(event.getId()).forEach(team -> team.applyScore(0));
        return new ScoreActivityResetResponse(removed);
    }

    private void recomputeScores(Event event) {
        var totals = new java.util.HashMap<UUID,Integer>();
        scoreEventRepository.findByEventIdOrderByCreatedAtDesc(event.getId()).forEach(e -> totals.merge(e.getTeam().getId(), e.getAmount(), Integer::sum));
        teamRepository.findByEventIdOrderByScoreDescNameAsc(event.getId()).forEach(team -> team.applyScore(Math.max(0, totals.getOrDefault(team.getId(), 0))));
    }

    private ScoreEventType determineEventType(int amount) {
        return amount > 0
                ? ScoreEventType.AWARD
                : ScoreEventType.DEDUCTION;
    }

    private Event findCurrentEvent() {
        return currentEvents.requireCurrent();
    }
}
