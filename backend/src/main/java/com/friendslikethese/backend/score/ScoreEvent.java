package com.friendslikethese.backend.score;

import com.friendslikethese.backend.event.Event;
import com.friendslikethese.backend.team.Team;
import com.friendslikethese.backend.round.GameRound;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "score_events")
public class ScoreEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "score_event_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private GameRound round;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "previous_score", nullable = false)
    private int previousScore;

    @Column(name = "new_score", nullable = false)
    private int newScore;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private ScoreEventType eventType;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ScoreEvent() {
    }

    public ScoreEvent(
            Event event,
            Team team,
            int amount,
            int previousScore,
            int newScore,
            String reason,
            ScoreEventType eventType
    ) {
        this(event, team, null, amount, previousScore, newScore, reason, eventType);
    }

    public ScoreEvent(Event event, Team team, GameRound round, int amount, int previousScore,
                      int newScore, String reason, ScoreEventType eventType) {
        this.event = event;
        this.team = team;
        this.round = round;
        this.amount = amount;
        this.previousScore = previousScore;
        this.newScore = newScore;
        this.reason = reason;
        this.eventType = eventType;
    }

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Team getTeam() {
        return team;
    }
    public Event getEvent() { return event; }
    public GameRound getRound() { return round; }

    public int getAmount() {
        return amount;
    }

    public int getPreviousScore() {
        return previousScore;
    }

    public int getNewScore() {
        return newScore;
    }

    public String getReason() {
        return reason;
    }

    public ScoreEventType getEventType() {
        return eventType;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
