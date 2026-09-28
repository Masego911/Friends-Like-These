package com.friendslikethese.backend.concurrency;

import com.friendslikethese.backend.common.BusinessRuleException;
import com.friendslikethese.backend.event.*;
import com.friendslikethese.backend.round.RoundService;
import com.friendslikethese.backend.score.*;
import com.friendslikethese.backend.team.Team;
import com.friendslikethese.backend.team.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ConcurrencyIntegrationTest {
    @Autowired EventService eventService;
    @Autowired EventRepository events;
    @Autowired TeamRepository teams;
    @Autowired ScoreService scores;
    @Autowired ScoreEventRepository scoreEvents;
    @Autowired RoundService rounds;

    @Test
    void concurrentScoreAdjustmentsAreBothAppliedWithMatchingLedger() throws Exception {
        Event event = events.findByCurrentTrue().orElseThrow();
        Team team = teams.saveAndFlush(new Team(event, "Concurrent Team"));
        eventService.closeRegistration();
        eventService.startGame();
        rounds.start(1);
        CyclicBarrier barrier = new CyclicBarrier(3);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> {
                barrier.await();
                return scores.adjustScore(team.getId(), new ScoreAdjustmentRequest(5, "Concurrent A"));
            });
            var second = executor.submit(() -> {
                barrier.await();
                return scores.adjustScore(team.getId(), new ScoreAdjustmentRequest(10, "Concurrent B"));
            });
            barrier.await(2, TimeUnit.SECONDS);
            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        Team stored = teams.findById(team.getId()).orElseThrow();
        List<ScoreEvent> ledger = scoreEvents.findByEventIdOrderByCreatedAtDesc(event.getId());
        assertEquals(15, stored.getScore());
        assertEquals(2, ledger.size());
        assertEquals(stored.getScore(), ledger.stream().mapToInt(ScoreEvent::getAmount).sum());
    }

    @Test
    void simultaneousStartGameAttemptsProduceOneTransitionAndOneBusinessRejection() throws Exception {
        eventService.closeRegistration();
        CyclicBarrier barrier = new CyclicBarrier(3);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> startAfterBarrier(barrier));
            var second = executor.submit(() -> startAfterBarrier(barrier));
            barrier.await(2, TimeUnit.SECONDS);
            List<Boolean> outcomes = List.of(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));
            assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count());
            assertEquals(1, outcomes.stream().filter(value -> !value).count());
        } finally {
            executor.shutdownNow();
        }
        assertEquals(EventStatus.LIVE, events.findByCurrentTrue().orElseThrow().getStatus());
        assertEquals(1, events.findAll().stream().filter(e -> e.getStatus() == EventStatus.LIVE).count());
    }

    @Test
    void concurrentNewGameRequestsPreserveSingleCurrentEvent() throws Exception {
        eventService.closeRegistration();
        eventService.startGame();
        eventService.completeGame();
        CyclicBarrier barrier = new CyclicBarrier(3);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> createAfterBarrier(barrier, "Next Game A"));
            var second = executor.submit(() -> createAfterBarrier(barrier, "Next Game B"));
            barrier.await(2, TimeUnit.SECONDS);
            List<Boolean> outcomes = List.of(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));
            assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count());
            assertEquals(1, outcomes.stream().filter(value -> !value).count());
        } finally {
            executor.shutdownNow();
        }
        assertEquals(1, events.countByCurrentTrue());
        assertEquals(2, events.count());
    }

    private boolean startAfterBarrier(CyclicBarrier barrier) {
        try {
            barrier.await();
            eventService.startGame();
            return true;
        } catch (BusinessRuleException exception) {
            return false;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private boolean createAfterBarrier(CyclicBarrier barrier, String name) {
        try {
            barrier.await();
            eventService.createEvent(new CreateEventRequest(name, LocalDate.now().plusDays(1),
                    OffsetDateTime.now().plusHours(12), 5));
            return true;
        } catch (BusinessRuleException exception) {
            return false;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
