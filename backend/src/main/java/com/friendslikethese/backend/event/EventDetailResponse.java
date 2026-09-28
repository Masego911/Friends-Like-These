package com.friendslikethese.backend.event;

import com.friendslikethese.backend.team.TeamResponse;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record EventDetailResponse(UUID id, String name, LocalDate eventDate, OffsetDateTime registrationDeadline,
                                  EventStatus status, int totalRounds, int currentRound, long teamCount,
                                  List<TeamResponse> teams) { }
