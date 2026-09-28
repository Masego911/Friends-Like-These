package com.friendslikethese.backend.event;

import com.friendslikethese.backend.common.BusinessRuleException;
import com.friendslikethese.backend.common.ResourceNotFoundException;
import com.friendslikethese.backend.registration.GoogleSheetsClient;
import com.friendslikethese.backend.registration.RegistrationImportService;
import com.friendslikethese.backend.score.*;
import com.friendslikethese.backend.team.Team;
import com.friendslikethese.backend.team.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EventLifecycleIntegrationTest {
    @Autowired EventService eventService;
    @Autowired EventRepository events;
    @Autowired TeamRepository teams;
    @Autowired ScoreEventRepository scoreEvents;
    @Autowired ScoreService scoreService;
    @Autowired RegistrationImportService registrationImportService;
    @MockitoBean GoogleSheetsClient sheets;

    @Test
    void completedGameRemainsQueryableAndSecondGameIsIsolated() {
        Event gameA = events.findByCurrentTrue().orElseThrow();
        Team finalist = new Team(gameA, "Finalists"); finalist.replaceMembers(List.of("Alex One", "Blair Two")); finalist.applyScore(120);
        finalist = teams.saveAndFlush(finalist);
        var finalistId = finalist.getId();
        Team withdrawn = new Team(gameA, "Withdrawn Team"); withdrawn.applyScore(50); withdrawn.softDelete();
        teams.saveAndFlush(withdrawn);
        scoreEvents.saveAndFlush(new ScoreEvent(gameA, finalist, 20, 100, 120, "Final answer", ScoreEventType.AWARD));

        eventService.closeRegistration();
        assertThrows(BusinessRuleException.class, eventService::closeRegistration);
        eventService.startGame();
        EventResponse completed = eventService.completeGame();

        assertEquals(EventStatus.COMPLETED, completed.status());
        assertFalse(completed.current());
        assertThrows(ResourceNotFoundException.class, eventService::getCurrentEvent);
        assertEquals(2, eventService.getEvent(gameA.getId()).teamCount());
        assertEquals(List.of("Finalists", "Withdrawn Team"),
                eventService.getLeaderboard(gameA.getId()).stream().map(e -> e.teamName()).toList());
        assertEquals(120, eventService.getLeaderboard(gameA.getId()).get(0).score());
        assertEquals(1, eventService.getScoreEvents(gameA.getId()).size());

        EventResponse gameB = eventService.createEvent(new CreateEventRequest("Game B", LocalDate.now().plusDays(7),
                OffsetDateTime.now().plusDays(6), 6));
        assertEquals(gameB.id(), eventService.getCurrentEvent().id());
        assertEquals(6, gameB.totalRounds());
        assertThrows(BusinessRuleException.class, registrationImportService::importRegistrations);
        assertThrows(BusinessRuleException.class, eventService::startGame);
        eventService.openRegistration(); eventService.closeRegistration(); eventService.startGame();

        assertThrows(BusinessRuleException.class,
                () -> scoreService.adjustScore(finalistId, new ScoreAdjustmentRequest(5, "Must fail")));
        assertEquals(120, teams.findById(finalistId).orElseThrow().getScore());
        assertEquals(1, eventService.getScoreEvents(gameA.getId()).size());
        assertEquals(2, eventService.getEvent(gameA.getId()).teamCount());
        assertEquals(0, eventService.getEvent(gameB.id()).teamCount());
    }

    @Test
    void anotherLiveEventPreventsStartingCurrentGame() {
        eventService.closeRegistration();
        Event rogue = new Event("Already Live", LocalDate.now(), OffsetDateTime.now(), EventStatus.LIVE);
        events.saveAndFlush(rogue);
        assertThrows(BusinessRuleException.class, eventService::startGame);
    }

    @Test
    void lifecycleRequiresEveryStateAndArchivedGameRemainsQueryable() {
        Event first = events.findByCurrentTrue().orElseThrow();
        eventService.closeRegistration();
        eventService.startGame();
        eventService.completeGame();

        EventResponse draft = eventService.createEvent(new CreateEventRequest("Lifecycle Game", LocalDate.now().plusDays(1),
                OffsetDateTime.now().plusHours(12), 5));
        assertEquals(EventStatus.DRAFT, draft.status());
        assertThrows(BusinessRuleException.class, eventService::closeRegistration);
        assertThrows(BusinessRuleException.class, eventService::startGame);
        assertThrows(BusinessRuleException.class, eventService::completeGame);

        assertEquals(EventStatus.REGISTRATION_OPEN, eventService.openRegistration().status());
        assertThrows(BusinessRuleException.class, eventService::openRegistration);
        assertThrows(BusinessRuleException.class, eventService::startGame);

        assertEquals(EventStatus.REGISTRATION_CLOSED, eventService.closeRegistration().status());
        assertThrows(BusinessRuleException.class, eventService::closeRegistration);
        assertThrows(BusinessRuleException.class, eventService::openRegistration);

        assertEquals(EventStatus.LIVE, eventService.startGame().status());
        assertThrows(BusinessRuleException.class, eventService::startGame);
        EventResponse completed = eventService.completeGame();
        assertEquals(EventStatus.COMPLETED, completed.status());
        assertEquals(EventStatus.ARCHIVED, eventService.archiveEvent(completed.id()).status());
        assertThrows(BusinessRuleException.class, () -> eventService.archiveEvent(completed.id()));
        assertEquals(EventStatus.ARCHIVED, eventService.getEvent(completed.id()).status());
        assertEquals(EventStatus.COMPLETED, eventService.getEvent(first.getId()).status());
    }
}
