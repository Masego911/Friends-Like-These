package com.friendslikethese.backend.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findByEventIdAndDeletedFalseOrderByScoreDescNameAsc(UUID eventId);
    Optional<Team> findByIdAndDeletedFalse(UUID id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Team t where t.id = :id and t.deleted = false")
    Optional<Team> findByIdAndDeletedFalseForUpdate(@Param("id") UUID id);
    Optional<Team> findByIdAndDeletedTrue(UUID id);
    boolean existsByEventIdAndNameIgnoreCaseAndDeletedFalse(UUID eventId, String name);
    Optional<Team> findByEventIdAndNameIgnoreCaseAndDeletedFalse(UUID eventId, String name);
    Optional<Team> findByEventIdAndNameIgnoreCase(UUID eventId, String name);
    List<Team> findByEventIdAndDeletedFalse(UUID eventId);
    boolean existsByEventIdAndNameIgnoreCaseAndIdNotAndDeletedFalse(UUID eventId, String name, UUID id);
    long countByEventId(UUID eventId);
    List<Team> findByEventIdOrderByScoreDescNameAsc(UUID eventId);
}
