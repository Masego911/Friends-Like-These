package com.friendslikethese.backend.event;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.time.LocalDate;

public record EventResponse(
        UUID id,
        String name,
        OffsetDateTime registrationDeadline,
        EventStatus status
        , int totalRounds
        , int currentRound
        , LocalDate eventDate
        , boolean current
) {
    public EventResponse(UUID id, String name, OffsetDateTime registrationDeadline, EventStatus status) {
        this(id, name, registrationDeadline, status, 5, 1,
                registrationDeadline == null ? null : registrationDeadline.toLocalDate(), false);
    }

    static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getRegistrationDeadline(),
                event.getStatus(),
                event.getTotalRounds(),
                event.getCurrentRound(),
                event.getEventDate(),
                event.isCurrent()
        );
    }
}
