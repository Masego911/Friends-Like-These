package com.friendslikethese.backend.config;

import com.friendslikethese.backend.event.EventStatus;
import com.friendslikethese.backend.event.EventRepository;
import com.friendslikethese.backend.round.GameRoundRepository;
import com.friendslikethese.backend.round.RoundService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Creates structural round rows only; legacy score events remain deliberately unassigned. */
@Component @Order(20)
public class ExistingRoundMigration implements ApplicationRunner {
 private final EventRepository events; private final GameRoundRepository rounds; private final RoundService service;
 public ExistingRoundMigration(EventRepository events,GameRoundRepository rounds,RoundService service){this.events=events;this.rounds=rounds;this.service=service;}
 @Override public void run(ApplicationArguments args){events.findByCurrentTrue().filter(e->e.getStatus()!=EventStatus.COMPLETED&&e.getStatus()!=EventStatus.ARCHIVED).ifPresent(e->{if(rounds.findByEventIdOrderByRoundNumber(e.getId()).isEmpty())service.reconcile(e);});}
}
