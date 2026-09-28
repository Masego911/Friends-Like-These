package com.friendslikethese.backend.registration;

import com.friendslikethese.backend.event.EventResponse;
import com.friendslikethese.backend.event.EventService;
import com.friendslikethese.backend.event.EventStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistrationSyncServiceTest {
    private final RegistrationImportService importer = mock(RegistrationImportService.class);
    private final EventService events = mock(EventService.class);
    private final GoogleSheetsProperties sheets = new GoogleSheetsProperties("sheet", "responses");
    private final RegistrationSyncProperties enabled =
            new RegistrationSyncProperties(true, Duration.ofSeconds(60), Duration.ofMinutes(2));

    @Test
    void automaticSyncImportsAndRecordsResult() {
        EventResponse event = openEvent();
        when(events.getCurrentEvent()).thenReturn(event);
        when(importer.importRegistrations(any())).thenReturn(new RegistrationImportResult(6, 1, 0, 5, 0, 0));
        var service = new RegistrationSyncService(importer, events, sheets, enabled);

        service.runAutomaticSync();

        verify(importer).importRegistrations(event.registrationDeadline());
        assertTrue(service.snapshot().lastSyncSuccessful());
        assertEquals(6, service.snapshot().result().rowsRead());
    }

    @Test
    void googleFailureDoesNotEscapeAndNextExecutionSucceeds() {
        when(events.getCurrentEvent()).thenReturn(openEvent());
        when(importer.importRegistrations(any()))
                .thenThrow(new RegistrationImportException("Google Sheets is temporarily unavailable."))
                .thenReturn(new RegistrationImportResult(1, 1, 0, 0, 0, 0));
        var service = new RegistrationSyncService(importer, events, sheets, enabled);

        assertDoesNotThrow(service::runAutomaticSync);
        assertFalse(service.snapshot().lastSyncSuccessful());
        assertDoesNotThrow(service::runAutomaticSync);
        assertTrue(service.snapshot().lastSyncSuccessful());
        assertNotNull(service.snapshot().lastSuccessfulSyncAt());
        assertEquals("Registration sync completed.", service.snapshot().lastSyncMessage());
        assertEquals(1, service.snapshot().result().rowsRead());
        assertEquals(1, service.snapshot().result().teamsCreated());
        assertEquals(2, mockingDetails(importer).getInvocations().size());
    }

    @Test
    void disabledAutomaticSyncDoesNotExecute() {
        var disabled = new RegistrationSyncProperties(false, Duration.ofSeconds(60), Duration.ofMinutes(2));
        new RegistrationSyncService(importer, events, sheets, disabled).runAutomaticSync();
        verifyNoInteractions(importer, events);
    }

    @Test
    void automaticAndManualSyncNeverEnterImporterTogether() throws Exception {
        when(events.getCurrentEvent()).thenReturn(openEvent());
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(importer.importRegistrations(any())).thenAnswer(invocation -> {
            entered.countDown();
            assertTrue(release.await(2, TimeUnit.SECONDS));
            return new RegistrationImportResult(1, 1, 0, 0, 0, 0);
        });
        when(importer.importRegistrations()).thenReturn(new RegistrationImportResult(1, 0, 0, 1, 0, 0));
        var service = new RegistrationSyncService(importer, events, sheets, enabled);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var automatic = executor.submit(service::runAutomaticSync);
            assertTrue(entered.await(1, TimeUnit.SECONDS));
            var manual = executor.submit(() -> assertThrows(RegistrationSyncInProgressException.class,
                    service::runManualSync));
            manual.get(2, TimeUnit.SECONDS);
            verify(importer, never()).importRegistrations();
            release.countDown();
            automatic.get(2, TimeUnit.SECONDS);
            verify(importer, never()).importRegistrations();
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void twoManualSyncRequestsDoNotWaitOrRunTogether() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(importer.importRegistrations()).thenAnswer(invocation -> {
            entered.countDown();
            assertTrue(release.await(2, TimeUnit.SECONDS));
            return new RegistrationImportResult(1, 1, 0, 0, 0, 0);
        });
        var service = new RegistrationSyncService(importer, events, sheets, enabled);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(service::runManualSync);
            assertTrue(entered.await(1, TimeUnit.SECONDS));
            var second = executor.submit(() -> assertThrows(RegistrationSyncInProgressException.class,
                    service::runManualSync));
            second.get(2, TimeUnit.SECONDS);
            release.countDown();
            first.get(2, TimeUnit.SECONDS);
            verify(importer, times(1)).importRegistrations();
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void repeatedAutomaticCyclesRunSequentiallyAndExposeLatestResult() {
        when(events.getCurrentEvent()).thenReturn(openEvent());
        when(importer.importRegistrations(any()))
                .thenReturn(new RegistrationImportResult(1, 1, 0, 0, 0, 0))
                .thenReturn(new RegistrationImportResult(1, 0, 0, 1, 0, 0));
        var service = new RegistrationSyncService(importer, events, sheets, enabled);

        service.runAutomaticSync();
        service.runAutomaticSync();

        verify(importer, times(2)).importRegistrations(any());
        assertEquals(1, service.snapshot().result().teamsUnchanged());
        assertEquals(0, service.snapshot().result().teamsCreated());
    }

    @Test
    void finalCatchUpRunsBrieflyAfterDeadlineButStopsAfterGracePeriod() {
        var service = new RegistrationSyncService(importer, events, sheets, enabled);
        OffsetDateTime now = OffsetDateTime.now();
        var recentlyClosed = new EventResponse(UUID.randomUUID(), "Event", now.minusSeconds(30), EventStatus.REGISTRATION_CLOSED);
        var longClosed = new EventResponse(UUID.randomUUID(), "Event", now.minusMinutes(3), EventStatus.REGISTRATION_CLOSED);
        assertFalse(service.shouldRun(recentlyClosed, now));
        assertFalse(service.shouldRun(longClosed, now));
    }

    private EventResponse openEvent() {
        return new EventResponse(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Event",
                OffsetDateTime.now().plusHours(1), EventStatus.REGISTRATION_OPEN);
    }
}
