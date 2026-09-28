package com.friendslikethese.backend.round;

import com.friendslikethese.backend.common.BusinessRuleException;
import com.friendslikethese.backend.common.ResourceNotFoundException;
import com.friendslikethese.backend.event.CurrentEventProvider;
import com.friendslikethese.backend.event.Event;
import com.friendslikethese.backend.score.ScoreEvent;
import com.friendslikethese.backend.score.ScoreEventRepository;
import com.friendslikethese.backend.team.Team;
import com.friendslikethese.backend.team.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class RoundService {
 private final GameRoundRepository rounds; private final CurrentEventProvider currentEvents;
 private final ScoreEventRepository scoreEvents; private final TeamRepository teams;
 public RoundService(GameRoundRepository rounds,CurrentEventProvider currentEvents,ScoreEventRepository scoreEvents,TeamRepository teams){this.rounds=rounds;this.currentEvents=currentEvents;this.scoreEvents=scoreEvents;this.teams=teams;}
 @Transactional public void reconcile(Event event){
  List<GameRound> existing=rounds.findByEventIdOrderByRoundNumber(event.getId());
  if(existing.stream().anyMatch(r->r.getStatus()!=RoundStatus.NOT_STARTED)) throw new BusinessRuleException("Total rounds cannot be changed after gameplay has started.");
  rounds.deleteByEventIdAndRoundNumberGreaterThan(event.getId(),event.getTotalRounds());
  Set<Integer> nums=new HashSet<>(); existing.forEach(r->nums.add(r.getRoundNumber()));
  for(int n=1;n<=event.getTotalRounds();n++) if(!nums.contains(n)) rounds.save(new GameRound(event,n));
 }
 @Transactional(readOnly=true) public List<RoundResponse> current(){return list(currentEvents.requireCurrent().getId());}
 @Transactional(readOnly=true) public List<RoundResponse> list(UUID eventId){return rounds.findByEventIdOrderByRoundNumber(eventId).stream().map(RoundResponse::from).toList();}
 @Transactional public synchronized RoundResponse start(int number){
  Event event=currentEvents.requireLive(); GameRound round=find(event.getId(),number);
  if(round.getStatus()!=RoundStatus.NOT_STARTED) throw new BusinessRuleException("Round is not waiting to start.");
  if(rounds.existsByEventIdAndStatus(event.getId(),RoundStatus.IN_PROGRESS)) throw new BusinessRuleException("Another round is already in progress.");
  if(number>1 && rounds.findByEventIdAndRoundNumber(event.getId(),number-1).map(r->r.getStatus()!=RoundStatus.COMPLETED).orElse(true)) throw new BusinessRuleException("Rounds must be played in order.");
  round.start(); event.selectRound(number); return RoundResponse.from(rounds.save(round));
 }
 @Transactional public RoundResponse complete(int number){
  Event event=currentEvents.requireLive(); GameRound round=find(event.getId(),number);
  if(round.getStatus()!=RoundStatus.IN_PROGRESS) throw new BusinessRuleException("Only the in-progress round can be completed.");
  round.complete(); return RoundResponse.from(rounds.save(round));
 }
 @Transactional(readOnly=true) public GameRound requireActive(Event event){return rounds.findByEventIdAndStatus(event.getId(),RoundStatus.IN_PROGRESS).orElseThrow(()->new BusinessRuleException("Start a round before changing scores."));}
 @Transactional(readOnly=true) public List<RoundStandingResponse> standings(UUID eventId,int number){
  GameRound round=find(eventId,number); Map<UUID,Integer> totals=new HashMap<>();
  for(ScoreEvent e:scoreEvents.findByRoundIdOrderByCreatedAtDesc(round.getId())) totals.merge(e.getTeam().getId(),e.getAmount(),Integer::sum);
  List<Team> ordered=new ArrayList<>(teams.findByEventIdOrderByScoreDescNameAsc(eventId));
  ordered.sort(Comparator.comparingInt((Team t)->totals.getOrDefault(t.getId(),0)).reversed().thenComparing(Team::getName,String.CASE_INSENSITIVE_ORDER));
  List<RoundStandingResponse> out=new ArrayList<>(); Integer prior=null; int rank=0;
  for(int i=0;i<ordered.size();i++){Team t=ordered.get(i);int score=totals.getOrDefault(t.getId(),0);if(prior==null||score!=prior)rank=i+1;out.add(new RoundStandingResponse(rank,t.getId(),t.getName(),score,t.getScore()));prior=score;}
  return out;
 }
 private GameRound find(UUID eventId,int number){return rounds.findByEventIdAndRoundNumber(eventId,number).orElseThrow(()->new ResourceNotFoundException("Round not found."));}
}
