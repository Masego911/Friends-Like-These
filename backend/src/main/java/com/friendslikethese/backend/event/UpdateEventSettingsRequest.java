package com.friendslikethese.backend.event;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.OffsetDateTime;

public record UpdateEventSettingsRequest(
        OffsetDateTime registrationDeadline,
        @Min(1) @Max(50) Integer totalRounds
) {
}
