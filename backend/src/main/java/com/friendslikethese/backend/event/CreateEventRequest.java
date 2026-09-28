package com.friendslikethese.backend.event;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CreateEventRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull LocalDate eventDate,
        @NotNull OffsetDateTime registrationDeadline,
        @NotNull @Min(1) @Max(50) Integer totalRounds
) { }
