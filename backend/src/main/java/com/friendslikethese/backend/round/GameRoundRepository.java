package com.friendslikethese.backend.round;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface GameRoundRepository extends JpaRepository<GameRound,UUID>{
 List<GameRound> findByEventIdOrderByRoundNumber(UUID eventId);
 Optional<GameRound> findByEventIdAndRoundNumber(UUID eventId,int roundNumber);
 Optional<GameRound> findByEventIdAndStatus(UUID eventId,RoundStatus status);
 boolean existsByEventIdAndStatus(UUID eventId,RoundStatus status);
 long countByEventIdAndStatus(UUID eventId,RoundStatus status);
 void deleteByEventIdAndRoundNumberGreaterThan(UUID eventId,int number);
}
