package com.friendslikethese.backend.registration;

import com.friendslikethese.backend.event.EventResponse;
import com.friendslikethese.backend.event.EventService;
import com.friendslikethese.backend.event.EventStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.friendslikethese.backend.common.ResourceNotFoundException;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RegistrationSyncService {
    private static final Logger log = LoggerFactory.getLogger(RegistrationSyncService.class);

    private final RegistrationImportService importService;
    private final EventService eventService;
    private final GoogleSheetsProperties sheetsProperties;
    private final RegistrationSyncProperties syncProperties;
    private final ReentrantLock importLock = new ReentrantLock();
    private final AtomicReference<RegistrationSyncSnapshot> snapshot =
            new AtomicReference<>(RegistrationSyncSnapshot.initial());

    public RegistrationSyncService(RegistrationImportService importService, EventService eventService,
                                   GoogleSheetsProperties sheetsProperties,
                                   RegistrationSyncProperties syncProperties) {
        this.importService = importService;
        this.eventService = eventService;
        this.sheetsProperties = sheetsProperties;
        this.syncProperties = syncProperties;
    }

    public void runAutomaticSync() {
        if (!syncProperties.enabled() || !sheetsProperties.isConfigured()) return;
        EventResponse event;
        try {
            event = eventService.getCurrentEvent();
        } catch (ResourceNotFoundException noCurrentGame) {
            return;
        } catch (RuntimeException exception) {
            recordFailure(exception);
            return;
        }
        if (!shouldRun(event, OffsetDateTime.now())) return;
        if (!importLock.tryLock()) {
            log.info("Registration auto-sync skipped: synchronization is already in progress");
            return;
        }
        try {
            log.info("Registration auto-sync started");
            execute(event.registrationDeadline());
        } catch (RuntimeException exception) {
            recordFailure(exception);
        } finally {
            importLock.unlock();
        }
    }

    public RegistrationImportResult runManualSync() {
        if (!importLock.tryLock()) throw new RegistrationSyncInProgressException();
        try {
            return execute(null);
        } catch (RuntimeException exception) {
            recordFailure(exception);
            throw exception;
        } finally {
            importLock.unlock();
        }
    }

    boolean shouldRun(EventResponse event, OffsetDateTime now) {
        if (event.status() != EventStatus.REGISTRATION_OPEN) return false;
        return event.registrationDeadline() == null
                || !now.isAfter(event.registrationDeadline().plus(syncProperties.closeGracePeriod()));
    }

    private RegistrationImportResult execute(OffsetDateTime acceptedThrough) {
        RegistrationImportResult result = acceptedThrough == null
                ? importService.importRegistrations()
                : importService.importRegistrations(acceptedThrough);
        OffsetDateTime now = OffsetDateTime.now();
        snapshot.set(new RegistrationSyncSnapshot(now, now, true, "Registration sync completed.", result));
        log.info("Registration sync completed: rowsRead={}, created={}, updated={}, unchanged={}, skipped={}, conflicts={}",
                result.rowsRead(), result.teamsCreated(), result.teamsUpdated(), result.teamsUnchanged(),
                result.rowsSkipped(), result.conflicts());
        return result;
    }

    private void recordFailure(RuntimeException exception) {
        RegistrationSyncSnapshot previous = snapshot.get();
        String message = exception instanceof RegistrationImportException
                ? exception.getMessage() : "Registration sync failed; the next run will retry.";
        snapshot.set(new RegistrationSyncSnapshot(OffsetDateTime.now(), previous.lastSuccessfulSyncAt(), false,
                message, previous.result()));
        log.warn("Registration sync failed; the next scheduled run will retry: {}", message);
    }

    public RegistrationSyncSnapshot snapshot() {
        return snapshot.get();
    }
}
