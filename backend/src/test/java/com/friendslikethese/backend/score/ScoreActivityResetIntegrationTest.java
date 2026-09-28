package com.friendslikethese.backend.score;

import com.friendslikethese.backend.event.Event;
import com.friendslikethese.backend.event.EventRepository;
import com.friendslikethese.backend.event.EventStatus;
import com.friendslikethese.backend.team.Team;
import com.friendslikethese.backend.team.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ScoreActivityResetIntegrationTest {
    @Autowired EventRepository events;
    @Autowired TeamRepository teams;
    @Autowired ScoreEventRepository scoreEvents;
    @Autowired ScoreService scoreService;

    @Test
    void clearsOnlyCurrentEventActivityWithoutChangingScoresAndEmptyResetIsSafe() {
        Event historical = events.findFirstByOrderByCreatedAtDesc().orElseThrow();
        historical.clearCurrent();
        events.saveAndFlush(historical);
        Team historicalTeam = teams.save(new Team(historical, "Historical Team"));
        scoreEvents.save(new ScoreEvent(historical, historicalTeam, 10, 0, 10, "Historical", ScoreEventType.AWARD));

        Event current = new Event("Current", OffsetDateTime.now().plusDays(1), EventStatus.LIVE);
        current.markCurrent();
        current = events.saveAndFlush(current);
        Team currentTeam = new Team(current, "Current Team");
        currentTeam.applyScore(120);
        currentTeam = teams.saveAndFlush(currentTeam);
        scoreEvents.saveAndFlush(new ScoreEvent(current, currentTeam, 20, 100, 120, "Current", ScoreEventType.AWARD));

        assertEquals(1, scoreService.resetScoreActivity().scoreEventsRemoved());
        assertEquals(120, teams.findById(currentTeam.getId()).orElseThrow().getScore());
        assertEquals(1, scoreEvents.findTop100ByEventIdOrderByCreatedAtDesc(historical.getId()).size());
        assertEquals(0, scoreEvents.findTop100ByEventIdOrderByCreatedAtDesc(current.getId()).size());
        assertEquals(0, scoreService.resetScoreActivity().scoreEventsRemoved());
    }
}
