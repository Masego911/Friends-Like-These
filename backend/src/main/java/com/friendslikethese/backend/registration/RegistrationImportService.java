package com.friendslikethese.backend.registration;

import com.friendslikethese.backend.team.TeamImportOutcome;
import com.friendslikethese.backend.team.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.time.OffsetDateTime;
import com.friendslikethese.backend.event.CurrentEventProvider;

@Service
public class RegistrationImportService {
    private final GoogleSheetsClient sheetsClient;
    private final TeamService teamService;
    private final CurrentEventProvider currentEvents;
    public RegistrationImportService(GoogleSheetsClient sheetsClient, TeamService teamService, CurrentEventProvider currentEvents) { this.sheetsClient = sheetsClient; this.teamService = teamService; this.currentEvents = currentEvents; }
    @Transactional
    public RegistrationImportResult importRegistrations() {
        return importRegistrations(null);
    }

    @Transactional
    public RegistrationImportResult importRegistrations(OffsetDateTime acceptedThrough) {
        currentEvents.requireRegistrationOpen();
        var batch = sheetsClient.fetchRegistrations();
        int created = 0, updated = 0, unchanged = 0, conflicts = 0, skipped = batch.rowsSkipped();
        Map<String, Registration> unique = new LinkedHashMap<>();
        for (Registration registration : batch.registrations()) {
            if (acceptedThrough != null && registration.submittedAt() != null
                    && registration.submittedAt().isAfter(acceptedThrough)) {
                skipped++;
                continue;
            }
            String name = normalize(registration.teamName());
            if (name.isBlank() || GoogleSheetsClient.isTeamNameHeader(name)) { skipped++; continue; }
            String key = name.toLowerCase(Locale.ROOT);
            List<String> members = clean(registration.members());
            Registration prior = unique.putIfAbsent(key, new Registration(name, members, registration.submittedAt()));
            if (prior != null) {
                skipped++;
                if (!prior.members().equals(members)) conflicts++;
                continue;
            }
            TeamImportOutcome outcome = teamService.importRegistration(name, members);
            if (outcome == TeamImportOutcome.CREATED) created++; else if (outcome == TeamImportOutcome.UPDATED) updated++; else unchanged++;
        }
        return new RegistrationImportResult(batch.rowsRead(), created, updated, unchanged, skipped, conflicts);
    }
    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
    private List<String> clean(List<String> members) {
        if (members == null) return List.of();
        return members.stream().filter(Objects::nonNull).map(String::trim).filter(m -> !m.isBlank()).toList();
    }
}
