package com.friendslikethese.backend.registration;

import com.friendslikethese.backend.event.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    private final RegistrationSyncService syncService;
    private final EventService eventService;
    private final String registrationUrl;
    private final RegistrationPreviewService previewService;
    private final GoogleSheetsProperties sheetsProperties;
    private final RegistrationSyncProperties syncProperties;
    public RegistrationController(RegistrationSyncService syncService, EventService eventService,
                                  @Value("${app.registration.form-url:https://docs.google.com/forms/d/e/1FAIpQLScrIKTorkwZWmBsr-S8YPjJObn4nW5E6aViWcyPOp6zyTLsEg/viewform}") String registrationUrl,
                                  RegistrationPreviewService previewService, GoogleSheetsProperties sheetsProperties,
                                  RegistrationSyncProperties syncProperties) { this.syncService = syncService; this.eventService = eventService; this.registrationUrl = registrationUrl; this.previewService = previewService; this.sheetsProperties = sheetsProperties; this.syncProperties = syncProperties; }
    @PostMapping("/import") public ResponseEntity<RegistrationImportResult> importRegistrations() {
        return ResponseEntity.ok(syncService.runManualSync());
    }
    @GetMapping("/preview") public ResponseEntity<RegistrationPreviewResponse> preview() { return ResponseEntity.ok(previewService.preview()); }
    @GetMapping("/status") public ResponseEntity<RegistrationStatusResponse> status() {
        var event = eventService.getCurrentEvent();
        var state = syncService.snapshot();
        var result = state.result();
        boolean open = event.status().name().equals("REGISTRATION_OPEN")
                && (event.registrationDeadline() == null || !OffsetDateTime.now().isAfter(event.registrationDeadline()));
        OffsetDateTime nextExpected = syncProperties.enabled() && state.lastSyncAt() != null
                ? state.lastSyncAt().plus(syncProperties.interval()) : null;
        return ResponseEntity.ok(new RegistrationStatusResponse(syncProperties.enabled(),
                syncProperties.interval().toSeconds(), state.lastSyncAt(), state.lastSuccessfulSyncAt(), nextExpected,
                state.lastSyncSuccessful(), state.lastSyncMessage(), result.rowsRead(), result.teamsCreated(),
                result.teamsUpdated(), result.teamsUnchanged(), result.rowsSkipped(), result.conflicts(), open,
                event.registrationDeadline(), registrationUrl, sheetsProperties.responseSheetUrl(),
                sheetsProperties.isConfigured() && state.lastSyncSuccessful()));
    }
}
