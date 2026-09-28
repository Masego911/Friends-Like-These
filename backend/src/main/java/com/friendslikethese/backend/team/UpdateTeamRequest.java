package com.friendslikethese.backend.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateTeamRequest(
        @NotBlank
        @Size(max = 120)
        String name,

        @NotEmpty
        List<@NotBlank @Size(max = 150) String> members
) {
}
