package com.friendslikethese.backend.team;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getTeams() {
        return ResponseEntity.ok(teamService.getTeams());
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.getTeam(teamId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard() {
        return ResponseEntity.ok(teamService.getLeaderboard());
    }

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(request));
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody UpdateTeamRequest request
    ) {
        return ResponseEntity.ok(teamService.updateTeam(teamId, request));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable UUID teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{teamId}/restore")
    public ResponseEntity<TeamResponse> restoreTeam(@PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.restoreTeam(teamId));
    }
}
