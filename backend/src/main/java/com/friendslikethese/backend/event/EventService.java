package com.friendslikethese.backend.event;

public interface EventService {

    EventResponse getCurrentEvent();

    EventResponse updateSettings(UpdateEventSettingsRequest request);
    EventResponse createEvent(CreateEventRequest request);
    EventResponse openRegistration();
    EventResponse closeRegistration();
    EventResponse startGame();
    EventResponse completeGame();
    EventResponse archiveEvent(java.util.UUID eventId);
    java.util.List<EventSummaryResponse> getEvents();
    EventDetailResponse getEvent(java.util.UUID eventId);
    java.util.List<com.friendslikethese.backend.team.LeaderboardEntryResponse> getLeaderboard(java.util.UUID eventId);
    java.util.List<com.friendslikethese.backend.score.ScoreEventResponse> getScoreEvents(java.util.UUID eventId);
}
