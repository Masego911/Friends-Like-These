package com.friendslikethese.backend.registration;

import com.friendslikethese.backend.event.EventResponse;
import com.friendslikethese.backend.event.EventService;
import com.friendslikethese.backend.event.EventStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistrationControllerTest {
    @Test
    void successfulScheduledSyncExposesLatestCountersAndGoogleAvailability() {
        EventService events = mock(EventService.class);
        RegistrationImportService importer = mock(RegistrationImportService.class);
        EventResponse event = new EventResponse(UUID.randomUUID(), "Friends Like These",
                OffsetDateTime.now().plusHours(1), EventStatus.REGISTRATION_OPEN);
        when(events.getCurrentEvent()).thenReturn(event);
        when(importer.importRegistrations(any())).thenReturn(new RegistrationImportResult(1, 0, 0, 1, 0, 0));
        GoogleSheetsProperties sheets = new GoogleSheetsProperties("sheet", "Form Responses 1");
        RegistrationSyncProperties sync = new RegistrationSyncProperties(true, Duration.ofSeconds(60), Duration.ofMinutes(2));
        RegistrationSyncService syncService = new RegistrationSyncService(importer, events, sheets, sync);
        syncService.runAutomaticSync();
        RegistrationController controller = new RegistrationController(syncService, events, "https://form.example",
                mock(RegistrationPreviewService.class), sheets, sync);

        RegistrationStatusResponse status = controller.status().getBody();

        assertNotNull(status);
        assertTrue(status.automaticSyncEnabled());
        assertTrue(status.googleIntegrationAvailable());
        assertTrue(status.lastSyncSuccessful());
        assertNotNull(status.lastSyncAt());
        assertNotNull(status.lastSuccessfulSyncAt());
        assertEquals(1, status.rowsRead());
        assertEquals(0, status.teamsCreated());
        assertEquals(1, status.teamsUnchanged());
        assertEquals(0, status.rowsSkipped());
        assertEquals(0, status.conflicts());
    }

    @Test
    void configuredIntegrationIsNotReportedAvailableBeforeASuccessfulRead() {
        EventService events = mock(EventService.class);
        EventResponse event = new EventResponse(UUID.randomUUID(), "Friends Like These",
                OffsetDateTime.now().plusHours(1), EventStatus.REGISTRATION_OPEN);
        when(events.getCurrentEvent()).thenReturn(event);
        GoogleSheetsProperties sheets = new GoogleSheetsProperties("sheet", "Form Responses 1");
        RegistrationSyncProperties sync = new RegistrationSyncProperties(true, Duration.ofSeconds(60), Duration.ofMinutes(2));
        RegistrationSyncService syncService = new RegistrationSyncService(mock(RegistrationImportService.class), events, sheets, sync);
        RegistrationController controller = new RegistrationController(syncService, events, "https://form.example",
                mock(RegistrationPreviewService.class), sheets, sync);

        RegistrationStatusResponse status = controller.status().getBody();

        assertNotNull(status);
        assertFalse(status.googleIntegrationAvailable());
        assertFalse(status.lastSyncSuccessful());
    }
}
