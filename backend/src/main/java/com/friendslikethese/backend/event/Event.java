package com.friendslikethese.backend.event;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "registration_deadline")
    private OffsetDateTime registrationDeadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "is_current")
    private Boolean current;

    @Column(name = "total_rounds")
    private Integer totalRounds;

    @Column(name = "current_round")
    private Integer currentRound;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Event() {
    }

    public Event(String name, OffsetDateTime registrationDeadline, EventStatus status) {
        this(name, registrationDeadline == null ? LocalDate.now() : registrationDeadline.toLocalDate(), registrationDeadline, status);
    }

    public Event(String name, LocalDate eventDate, OffsetDateTime registrationDeadline, EventStatus status) {
        this.name = name;
        this.eventDate = eventDate;
        this.registrationDeadline = registrationDeadline;
        this.status = status;
        this.current = false;
        this.totalRounds = 5;
        this.currentRound = 1;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public EventStatus getStatus() {
        return status;
    }

    public LocalDate getEventDate() {
        return eventDate == null ? createdAt.toLocalDate() : eventDate;
    }

    public boolean isCurrent() {
        return Boolean.TRUE.equals(current);
    }

    public int getTotalRounds() {
        return totalRounds == null ? 5 : totalRounds;
    }

    public int getCurrentRound() {
        return currentRound == null ? 1 : currentRound;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateRegistrationDeadline(OffsetDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public void updateStatus(EventStatus status) {
        this.status = status;
    }

    public void markCurrent() { current = true; }

    public void clearCurrent() { current = false; }

    public void start() {
        status = EventStatus.LIVE;
        currentRound = 1;
    }

    public void selectRound(int roundNumber) { this.currentRound = roundNumber; }

    public void updateTotalRounds(int totalRounds) {
        if (totalRounds < 1 || totalRounds > 50) {
            throw new IllegalArgumentException("Total rounds must be between 1 and 50.");
        }
        this.totalRounds = totalRounds;
        if (currentRound == null || currentRound < 1) currentRound = 1;
        if (currentRound > totalRounds) currentRound = totalRounds;
    }
}
