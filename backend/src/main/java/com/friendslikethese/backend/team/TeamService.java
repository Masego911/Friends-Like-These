package com.friendslikethese.backend.team;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    List<TeamResponse> getTeams();
    TeamResponse getTeam(UUID teamId);
    List<LeaderboardEntryResponse> getLeaderboard();
    TeamResponse createTeam(CreateTeamRequest request);
    TeamResponse updateTeam(UUID teamId, UpdateTeamRequest request);
    void deleteTeam(UUID teamId);
    TeamResponse restoreTeam(UUID teamId);
    TeamImportOutcome importRegistration(String name, List<String> members);
}
