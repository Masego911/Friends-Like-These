package com.friendslikethese.backend.score;

import com.friendslikethese.backend.team.TeamResponse;

import java.util.List;
import java.util.UUID;

public interface ScoreService {

    TeamResponse adjustScore(UUID teamId, ScoreAdjustmentRequest request);

    List<ScoreEventResponse> getScoreHistory();

    void resetAllScores();

    ScoreActivityResetResponse resetScoreActivity();
    ScoreActivityResetResponse resetEntireGame();
}
