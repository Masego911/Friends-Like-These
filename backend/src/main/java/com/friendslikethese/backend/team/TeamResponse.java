package com.friendslikethese.backend.team;

import java.util.List;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        List<String> members,
        int score
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getMembers()
                        .stream()
                        .map(TeamMember::getFullName)
                        .toList(),
                team.getScore()
        );
    }
}
