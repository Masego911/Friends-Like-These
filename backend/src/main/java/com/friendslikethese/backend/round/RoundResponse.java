package com.friendslikethese.backend.round;
import java.time.OffsetDateTime;import java.util.UUID;
public record RoundResponse(UUID id,UUID eventId,int roundNumber,String name,RoundStatus status,OffsetDateTime startedAt,OffsetDateTime completedAt){
 public static RoundResponse from(GameRound r){return new RoundResponse(r.getId(),r.getEvent().getId(),r.getRoundNumber(),r.getName(),r.getStatus(),r.getStartedAt(),r.getCompletedAt());}
}
