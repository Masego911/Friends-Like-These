package com.friendslikethese.backend.event;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/current")
    public ResponseEntity<EventResponse> getCurrentEvent() {
        return ResponseEntity.ok(eventService.getCurrentEvent());
    }

    @PatchMapping("/current/settings")
    public ResponseEntity<EventResponse> updateSettings(
            @Valid @RequestBody UpdateEventSettingsRequest request
    ) {
        return ResponseEntity.ok(eventService.updateSettings(request));
    }

    @GetMapping public ResponseEntity<java.util.List<EventSummaryResponse>> getEvents() { return ResponseEntity.ok(eventService.getEvents()); }
    @PostMapping public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) { return ResponseEntity.status(201).body(eventService.createEvent(request)); }
    @PostMapping("/current/open-registration") public ResponseEntity<EventResponse> openRegistration() { return ResponseEntity.ok(eventService.openRegistration()); }
    @PostMapping("/current/close-registration") public ResponseEntity<EventResponse> closeRegistration() { return ResponseEntity.ok(eventService.closeRegistration()); }
    @PostMapping("/current/start") public ResponseEntity<EventResponse> startGame() { return ResponseEntity.ok(eventService.startGame()); }
    @PostMapping("/current/complete") public ResponseEntity<EventResponse> completeGame() { return ResponseEntity.ok(eventService.completeGame()); }
    @PostMapping("/{eventId}/archive") public ResponseEntity<EventResponse> archive(@PathVariable java.util.UUID eventId) { return ResponseEntity.ok(eventService.archiveEvent(eventId)); }
    @GetMapping("/{eventId}") public ResponseEntity<EventDetailResponse> getEvent(@PathVariable java.util.UUID eventId) { return ResponseEntity.ok(eventService.getEvent(eventId)); }
    @GetMapping("/{eventId}/leaderboard") public ResponseEntity<java.util.List<com.friendslikethese.backend.team.LeaderboardEntryResponse>> leaderboard(@PathVariable java.util.UUID eventId) { return ResponseEntity.ok(eventService.getLeaderboard(eventId)); }
    @GetMapping("/{eventId}/score-events") public ResponseEntity<java.util.List<com.friendslikethese.backend.score.ScoreEventResponse>> scoreEvents(@PathVariable java.util.UUID eventId) { return ResponseEntity.ok(eventService.getScoreEvents(eventId)); }
}
