package com.friendslikethese.backend.registration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RegistrationSyncScheduler {
    private final RegistrationSyncService syncService;

    public RegistrationSyncScheduler(RegistrationSyncService syncService) {
        this.syncService = syncService;
    }

    @Scheduled(fixedDelayString = "${app.registration.sync.interval:PT60S}")
    public void syncRegistrations() {
        syncService.runAutomaticSync();
    }
}
