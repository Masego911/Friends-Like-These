package com.friendslikethese.backend.score;

import com.friendslikethese.backend.common.BusinessRuleException;
import com.friendslikethese.backend.event.Event;
import com.friendslikethese.backend.event.EventRepository;
import com.friendslikethese.backend.event.EventService;
import com.friendslikethese.backend.round.RoundService;
import com.friendslikethese.backend.team.Team;
import com.friendslikethese.backend.team.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ScoreAdjustmentRegressionTest {
    @Autowired EventRepository events;
    @Autowired EventService eventService;
    @Autowired RoundService rounds;
    @Autowired TeamRepository teams;
    @Autowired ScoreService scores;
    @Autowired ScoreEventRepository scoreEvents;

    @Test
    void scoringUsesNumericDeltasInLiveGameAndPersistsMatchingLedger() {
        Event event = events.findByCurrentTrue().orElseThrow();
        Team team = teams.saveAndFlush(new Team(event, "Score Regression Team"));

        assertThrows(BusinessRuleException.class,
                () -> scores.adjustScore(team.getId(), new ScoreAdjustmentRequest(5, "Blocked before live")));
        assertEquals(0, teams.findById(team.getId()).orElseThrow().getScore());
        assertEquals(0, scoreEvents.findByEventIdOrderByCreatedAtDesc(event.getId()).size());

        eventService.closeRegistration();
        eventService.startGame();
        rounds.start(1);

        assertEquals(1, scores.adjustScore(team.getId(), new ScoreAdjustmentRequest(1, "+1")).score());
        assertEquals(6, scores.adjustScore(team.getId(), new ScoreAdjustmentRequest(5, "+5")).score());
        assertEquals(16, scores.adjustScore(team.getId(), new ScoreAdjustmentRequest(10, "+10")).score());
        assertEquals(11, scores.adjustScore(team.getId(), new ScoreAdjustmentRequest(-5, "negative")).score());
        assertEquals(18, scores.adjustScore(team.getId(), new ScoreAdjustmentRequest(7, "custom add")).score());
        assertEquals(15, scores.adjustScore(team.getId(), new ScoreAdjustmentRequest(-3, "custom deduct")).score());

        Team stored = teams.findById(team.getId()).orElseThrow();
        List<ScoreEvent> ledger = scoreEvents.findByEventIdOrderByCreatedAtDesc(event.getId());
        assertEquals(15, stored.getScore());
        assertEquals(6, ledger.size());
        assertEquals(stored.getScore(), ledger.stream().mapToInt(ScoreEvent::getAmount).sum());
    }
}
