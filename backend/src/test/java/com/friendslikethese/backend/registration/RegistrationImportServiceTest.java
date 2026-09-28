package com.friendslikethese.backend.registration;

import com.friendslikethese.backend.team.TeamImportOutcome;
import com.friendslikethese.backend.team.TeamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.friendslikethese.backend.event.CurrentEventProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationImportServiceTest {
    @Mock GoogleSheetsClient client;
    @Mock TeamService teamService;
    @Mock CurrentEventProvider currentEvents;

    @Test void importsRowsAndReportsCounts() {
        var first = new Registration(" Code Warriors ", List.of(" Alice ", "Bob"));
        var second = new Registration("Quiz Team", List.of());
        when(client.fetchRegistrations()).thenReturn(new RegistrationBatch(4, 2, List.of(first, second)));
        when(teamService.importRegistration("Code Warriors", List.of("Alice", "Bob"))).thenReturn(TeamImportOutcome.CREATED);
        when(teamService.importRegistration("Quiz Team", List.of())).thenReturn(TeamImportOutcome.UNCHANGED);

        var result = new RegistrationImportService(client, teamService, currentEvents).importRegistrations();

        assertEquals(new RegistrationImportResult(4, 1, 0, 1, 2), result);
        verify(teamService).importRegistration("Code Warriors", List.of("Alice", "Bob"));
    }

    @Test void propagatesControlledSheetsFailure() {
        when(client.fetchRegistrations()).thenThrow(new RegistrationImportException("Google Sheets is temporarily unavailable.", new RuntimeException()));
        assertThrows(RegistrationImportException.class, () -> new RegistrationImportService(client, teamService, currentEvents).importRegistrations());
        verifyNoInteractions(teamService);
    }

    @Test void ignoresHeadersAndBlanksAndClassifiesDuplicateSheetRows() {
        var registrations = List.of(
                new Registration("Team Name", List.of("Member 1")),
                new Registration(" ", List.of("Nobody")),
                new Registration(" Alpha ", List.of("Alice")),
                new Registration("alpha", List.of("Different Member")),
                new Registration("ALPHA", List.of("Alice")));
        when(client.fetchRegistrations()).thenReturn(new RegistrationBatch(5, 0, registrations));
        when(teamService.importRegistration("Alpha", List.of("Alice"))).thenReturn(TeamImportOutcome.CREATED);

        var result = new RegistrationImportService(client, teamService, currentEvents).importRegistrations();

        assertEquals(new RegistrationImportResult(5, 1, 0, 0, 4, 1), result);
        verify(teamService, times(1)).importRegistration(anyString(), anyList());
    }
}
