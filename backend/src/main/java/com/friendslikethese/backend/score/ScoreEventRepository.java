package com.friendslikethese.backend.score;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScoreEventRepository extends JpaRepository<ScoreEvent, UUID> {

    List<ScoreEvent> findTop100ByEventIdOrderByCreatedAtDesc(UUID eventId);
    long deleteByEventId(UUID eventId);
    List<ScoreEvent> findByEventIdOrderByCreatedAtDesc(UUID eventId);
    List<ScoreEvent> findByRoundIdOrderByCreatedAtDesc(UUID roundId);
    long deleteByRoundId(UUID roundId);
}
