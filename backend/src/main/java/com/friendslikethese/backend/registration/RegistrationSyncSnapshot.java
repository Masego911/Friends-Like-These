package com.friendslikethese.backend.registration;

import java.time.OffsetDateTime;

public record RegistrationSyncSnapshot(
        OffsetDateTime lastSyncAt,
        OffsetDateTime lastSuccessfulSyncAt,
        boolean lastSyncSuccessful,
        String lastSyncMessage,
        RegistrationImportResult result
) {
    static RegistrationSyncSnapshot initial() {
        return new RegistrationSyncSnapshot(null, null, false, "No sync has run yet.",
                new RegistrationImportResult(0, 0, 0, 0, 0, 0));
    }
}
