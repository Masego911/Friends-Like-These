package com.friendslikethese.backend.team;

import java.util.UUID;

public record LeaderboardEntryResponse(
        int position,
        UUID teamId,
        String teamName,
        int score
) {
}
