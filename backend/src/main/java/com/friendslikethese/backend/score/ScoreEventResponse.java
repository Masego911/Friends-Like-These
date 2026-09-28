package com.friendslikethese.backend.score;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScoreEventResponse(
        UUID id,
        UUID teamId,
        String teamName,
        int amount,
        int previousScore,
        int newScore,
        String reason,
        ScoreEventType eventType,
        OffsetDateTime timestamp,
        UUID roundId,
        Integer roundNumber,
        String roundName
) {
    public static ScoreEventResponse from(ScoreEvent scoreEvent) {
        return new ScoreEventResponse(
                scoreEvent.getId(),
                scoreEvent.getTeam().getId(),
                scoreEvent.getTeam().getName(),
                scoreEvent.getAmount(),
                scoreEvent.getPreviousScore(),
                scoreEvent.getNewScore(),
                scoreEvent.getReason(),
                scoreEvent.getEventType(),
                scoreEvent.getCreatedAt(),
                scoreEvent.getRound() == null ? null : scoreEvent.getRound().getId(),
                scoreEvent.getRound() == null ? null : scoreEvent.getRound().getRoundNumber(),
                scoreEvent.getRound() == null ? null : scoreEvent.getRound().getName()
        );
    }
}
