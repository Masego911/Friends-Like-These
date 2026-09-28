package com.friendslikethese.backend.score;

import com.friendslikethese.backend.team.TeamResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @PostMapping("/teams/{teamId}/score")
    public ResponseEntity<TeamResponse> adjustScore(
            @PathVariable UUID teamId,
            @Valid @RequestBody ScoreAdjustmentRequest request
    ) {
        return ResponseEntity.ok(scoreService.adjustScore(teamId, request));
    }

    @GetMapping("/score-events")
    public ResponseEntity<List<ScoreEventResponse>> getScoreHistory() {
        return ResponseEntity.ok(scoreService.getScoreHistory());
    }

    @PostMapping("/teams/scores/reset")
    public ResponseEntity<Void> resetAllScores() {
        scoreService.resetAllScores();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/score-events/current-game")
    public ResponseEntity<ScoreActivityResetResponse> resetEntireGame() {
        return ResponseEntity.ok(scoreService.resetEntireGame());
    }

    @DeleteMapping("/score-events/current")
    public ResponseEntity<ScoreActivityResetResponse> resetScoreActivity() {
        return ResponseEntity.ok(scoreService.resetScoreActivity());
    }
}
