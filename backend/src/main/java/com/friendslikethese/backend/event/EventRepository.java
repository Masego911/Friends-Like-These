package com.friendslikethese.backend.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, UUID> {

    Optional<Event> findFirstByOrderByCreatedAtDesc();
    Optional<Event> findByCurrentTrue();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.current = true")
    Optional<Event> findCurrentForUpdate();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") UUID id);
    boolean existsByCurrentTrue();
    long countByCurrentTrue();
    boolean existsByStatus(EventStatus status);
    List<Event> findAllByOrderByEventDateDescCreatedAtDesc();
}
