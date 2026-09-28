package com.friendslikethese.backend.registration;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RegistrationSyncSchedulerTest {
    @Test
    void schedulerInvokesSyncService() {
        RegistrationSyncService service = mock(RegistrationSyncService.class);
        new RegistrationSyncScheduler(service).syncRegistrations();
        verify(service).runAutomaticSync();
    }
}
