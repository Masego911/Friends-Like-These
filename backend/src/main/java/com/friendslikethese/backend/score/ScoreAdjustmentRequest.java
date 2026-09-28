package com.friendslikethese.backend.score;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScoreAdjustmentRequest(
        int amount,

        @NotBlank
        @Size(max = 255)
        String reason
) {
}
