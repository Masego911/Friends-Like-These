package com.friendslikethese.backend.round;

import com.friendslikethese.backend.common.BusinessRuleException;
import com.friendslikethese.backend.event.*;
import com.friendslikethese.backend.score.*;
import com.friendslikethese.backend.team.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @Transactional
class RoundEngineIntegrationTest {
 @Autowired EventService events; @Autowired EventRepository eventRepository; @Autowired RoundService rounds;
 @Autowired GameRoundRepository roundRepository; @Autowired TeamRepository teams; @Autowired ScoreService scores;
 @Autowired ScoreEventRepository scoreEvents;

 @Test void threeRoundLedgerAccumulatesAndRoundResetPreservesEarlierRound(){
  Event event=eventRepository.findByCurrentTrue().orElseThrow();
  events.updateSettings(new UpdateEventSettingsRequest(null,3));
  events.updateSettings(new UpdateEventSettingsRequest(null,3));
  assertEquals(List.of(1,2,3),rounds.current().stream().map(RoundResponse::roundNumber).toList());
  Team a=teams.save(new Team(event,"Round Team A")), b=teams.save(new Team(event,"Round Team B"));
  events.closeRegistration(); events.startGame();
  assertThrows(BusinessRuleException.class,()->scores.adjustScore(a.getId(),new ScoreAdjustmentRequest(5,"too early")));
  assertThrows(BusinessRuleException.class,()->rounds.start(2));
  rounds.start(1); scores.adjustScore(a.getId(),new ScoreAdjustmentRequest(20,"R1")); scores.adjustScore(b.getId(),new ScoreAdjustmentRequest(10,"R1")); rounds.complete(1);
  assertThrows(BusinessRuleException.class,()->scores.adjustScore(a.getId(),new ScoreAdjustmentRequest(5,"late")));
  assertEquals(List.of(20,10),rounds.standings(event.getId(),1).stream().map(RoundStandingResponse::roundScore).toList());
  rounds.start(2); assertEquals(List.of(0,0),rounds.standings(event.getId(),2).stream().map(RoundStandingResponse::roundScore).toList());
  scores.adjustScore(a.getId(),new ScoreAdjustmentRequest(10,"R2")); scores.adjustScore(b.getId(),new ScoreAdjustmentRequest(30,"R2"));
  assertEquals(30,teams.findById(a.getId()).orElseThrow().getScore()); assertEquals(40,teams.findById(b.getId()).orElseThrow().getScore());
  scores.resetAllScores();
  assertEquals(20,teams.findById(a.getId()).orElseThrow().getScore()); assertEquals(10,teams.findById(b.getId()).orElseThrow().getScore());
  assertEquals(List.of(20,10),rounds.standings(event.getId(),1).stream().map(RoundStandingResponse::roundScore).toList());
  assertEquals(List.of(0,0),rounds.standings(event.getId(),2).stream().map(RoundStandingResponse::roundScore).toList());
 }

 @Test void activityAndEntireGameResetsAreScopedAndTiesUseCompetitionRanking(){
  Event event=eventRepository.findByCurrentTrue().orElseThrow(); events.updateSettings(new UpdateEventSettingsRequest(null,2));
  Team a=teams.save(new Team(event,"Tie A")),b=teams.save(new Team(event,"Tie B")); events.closeRegistration();events.startGame();rounds.start(1);
  scores.adjustScore(a.getId(),new ScoreAdjustmentRequest(10,"tie"));scores.adjustScore(b.getId(),new ScoreAdjustmentRequest(10,"tie"));
  var board=rounds.standings(event.getId(),1);assertEquals(1,board.get(0).position());assertEquals(1,board.get(1).position());
  scores.resetScoreActivity(); assertEquals(0,teams.findById(a.getId()).orElseThrow().getScore()); assertTrue(scoreEvents.findByEventIdOrderByCreatedAtDesc(event.getId()).isEmpty());
  scores.adjustScore(a.getId(),new ScoreAdjustmentRequest(15,"again")); scores.resetEntireGame();
  assertEquals(0,teams.findById(a.getId()).orElseThrow().getScore());assertTrue(scoreEvents.findByEventIdOrderByCreatedAtDesc(event.getId()).isEmpty());
 }

 @Test void completedRoundIsHistoricallyQueryableAndSettingsLockAfterStart(){
  Event event=eventRepository.findByCurrentTrue().orElseThrow();events.updateSettings(new UpdateEventSettingsRequest(null,1));
  events.closeRegistration();events.startGame();rounds.start(1);
  assertThrows(BusinessRuleException.class,()->events.updateSettings(new UpdateEventSettingsRequest(null,2)));
  rounds.complete(1);events.completeGame();
  assertEquals(RoundStatus.COMPLETED,rounds.list(event.getId()).get(0).status());
 }
}
