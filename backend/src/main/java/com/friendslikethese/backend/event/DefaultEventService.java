package com.friendslikethese.backend.event;

import com.friendslikethese.backend.common.BusinessRuleException;
import com.friendslikethese.backend.common.ResourceNotFoundException;
import com.friendslikethese.backend.score.ScoreEventRepository;
import com.friendslikethese.backend.score.ScoreEventResponse;
import com.friendslikethese.backend.round.GameRoundRepository;
import com.friendslikethese.backend.round.RoundService;
import com.friendslikethese.backend.round.RoundStatus;
import com.friendslikethese.backend.team.LeaderboardEntryResponse;
import com.friendslikethese.backend.team.Team;
import com.friendslikethese.backend.team.TeamRepository;
import com.friendslikethese.backend.team.TeamResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class DefaultEventService implements EventService {
    private final EventRepository events;
    private final TeamRepository teams;
    private final ScoreEventRepository scoreEvents;
    private final CurrentEventProvider currentEvents;
    private final GameRoundRepository rounds;
    private final RoundService roundService;
    private final ReentrantLock createEventLock = new ReentrantLock();

    public DefaultEventService(EventRepository events, TeamRepository teams,
                               ScoreEventRepository scoreEvents, CurrentEventProvider currentEvents,
                               GameRoundRepository rounds, RoundService roundService) {
        this.events = events; this.teams = teams; this.scoreEvents = scoreEvents; this.currentEvents = currentEvents;
        this.rounds = rounds; this.roundService = roundService;
    }

    @Override @Transactional(readOnly = true)
    public EventResponse getCurrentEvent() { return EventResponse.from(currentEvents.requireCurrent()); }

    @Override @Transactional
    public EventResponse updateSettings(UpdateEventSettingsRequest request) {
        Event event = currentEvents.requireMutable();
        if (request.registrationDeadline() != null) event.updateRegistrationDeadline(request.registrationDeadline());
        if (request.totalRounds() != null && request.totalRounds() != event.getTotalRounds()) {
            if (event.getStatus() == EventStatus.LIVE || rounds.findByEventIdOrderByRoundNumber(event.getId()).stream().anyMatch(r -> r.getStatus() != RoundStatus.NOT_STARTED))
                throw new BusinessRuleException("Total rounds cannot be changed after gameplay has started.");
            event.updateTotalRounds(request.totalRounds()); events.save(event); roundService.reconcile(event);
        }
        return EventResponse.from(events.save(event));
    }

    @Override @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        createEventLock.lock();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) { createEventLock.unlock(); }
        });
        try {
            if (events.existsByCurrentTrue()) throw new BusinessRuleException("Complete the current game before creating a new game.");
            Event event = new Event(request.name().trim(), request.eventDate(), request.registrationDeadline(), EventStatus.DRAFT);
            event.updateTotalRounds(request.totalRounds()); event.markCurrent(); events.saveAndFlush(event); roundService.reconcile(event);
            return EventResponse.from(event);
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    @Override @Transactional public EventResponse openRegistration() { return transition(EventStatus.DRAFT, EventStatus.REGISTRATION_OPEN); }
    @Override @Transactional public EventResponse closeRegistration() { return transition(EventStatus.REGISTRATION_OPEN, EventStatus.REGISTRATION_CLOSED); }

    @Override @Transactional
    public EventResponse startGame() {
        Event event = currentEvents.requireMutable(); requireStatus(event, EventStatus.REGISTRATION_CLOSED);
        if (events.existsByStatus(EventStatus.LIVE)) throw new BusinessRuleException("Only one game may be LIVE at a time.");
        if (rounds.countByEventIdAndStatus(event.getId(), RoundStatus.NOT_STARTED) != event.getTotalRounds()) roundService.reconcile(event);
        event.start(); return EventResponse.from(events.save(event));
    }

    @Override @Transactional
    public EventResponse completeGame() {
        Event event = currentEvents.requireLive();
        event.updateStatus(EventStatus.COMPLETED); event.clearCurrent();
        return EventResponse.from(events.save(event));
    }

    @Override @Transactional
    public EventResponse archiveEvent(UUID eventId) {
        Event event = events.findByIdForUpdate(eventId).orElseThrow(() -> new ResourceNotFoundException("Game not found."));
        requireStatus(event, EventStatus.COMPLETED);
        event.updateStatus(EventStatus.ARCHIVED); event.clearCurrent();
        return EventResponse.from(events.saveAndFlush(event));
    }

    private EventResponse transition(EventStatus expected, EventStatus target) {
        Event event = currentEvents.requireMutable(); requireStatus(event, expected);
        event.updateStatus(target); return EventResponse.from(events.save(event));
    }

    private void requireStatus(Event event, EventStatus expected) {
        if (event.getStatus() != expected) throw new BusinessRuleException("Game must be " + expected + " for this action.");
    }

    @Override @Transactional(readOnly = true)
    public List<EventSummaryResponse> getEvents() {
        return events.findAllByOrderByEventDateDescCreatedAtDesc().stream()
                .map(e -> new EventSummaryResponse(e.getId(), e.getName(), e.getEventDate(), e.getStatus(),
                        e.getTotalRounds(), e.getCurrentRound(), teams.countByEventId(e.getId()), e.isCurrent()))
                .toList();
    }

    @Override @Transactional(readOnly = true)
    public EventDetailResponse getEvent(UUID eventId) {
        Event event = find(eventId); List<Team> eventTeams = eventTeams(event);
        return new EventDetailResponse(event.getId(), event.getName(), event.getEventDate(), event.getRegistrationDeadline(),
                event.getStatus(), event.getTotalRounds(), event.getCurrentRound(), eventTeams.size(),
                eventTeams.stream().map(TeamResponse::from).toList());
    }

    @Override @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard(UUID eventId) {
        List<Team> ordered = eventTeams(find(eventId)); List<LeaderboardEntryResponse> result = new ArrayList<>();
        Integer priorScore = null; int position = 0;
        for (int index = 0; index < ordered.size(); index++) {
            Team team = ordered.get(index);
            if (priorScore == null || team.getScore() != priorScore) position = index + 1;
            result.add(new LeaderboardEntryResponse(position, team.getId(), team.getName(), team.getScore()));
            priorScore = team.getScore();
        }
        return result;
    }

    @Override @Transactional(readOnly = true)
    public List<ScoreEventResponse> getScoreEvents(UUID eventId) {
        find(eventId);
        return scoreEvents.findByEventIdOrderByCreatedAtDesc(eventId).stream().map(ScoreEventResponse::from).toList();
    }

    private List<Team> eventTeams(Event event) {
        return event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.ARCHIVED
                ? teams.findByEventIdOrderByScoreDescNameAsc(event.getId())
                : teams.findByEventIdAndDeletedFalseOrderByScoreDescNameAsc(event.getId());
    }

    private Event find(UUID id) {
        return events.findById(id).orElseThrow(() -> new ResourceNotFoundException("Game not found."));
    }
}
