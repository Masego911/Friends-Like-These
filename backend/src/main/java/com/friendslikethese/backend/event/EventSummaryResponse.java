package com.friendslikethese.backend.event;

import java.time.LocalDate;
import java.util.UUID;

public record EventSummaryResponse(UUID id, String name, LocalDate eventDate, EventStatus status,
                                   int totalRounds, int currentRound, long teamCount, boolean current) { }
