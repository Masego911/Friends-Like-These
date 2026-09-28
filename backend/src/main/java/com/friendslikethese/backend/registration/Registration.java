package com.friendslikethese.backend.registration;

import java.util.List;
import java.time.OffsetDateTime;

public record Registration(String teamName, List<String> members, OffsetDateTime submittedAt) {
    public Registration(String teamName, List<String> members) {
        this(teamName, members, null);
    }
}
