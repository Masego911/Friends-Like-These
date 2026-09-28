package com.friendslikethese.backend.registration;

import java.time.OffsetDateTime;

public record RegistrationStatusResponse(
        boolean automaticSyncEnabled, long syncIntervalSeconds, OffsetDateTime lastSyncAt,
        OffsetDateTime lastSuccessfulSyncAt, OffsetDateTime nextExpectedSyncAt,
        boolean lastSyncSuccessful, String lastSyncMessage, int rowsRead,
        int teamsCreated, int teamsUpdated, int teamsUnchanged, int rowsSkipped, int conflicts,
        boolean registrationOpen, OffsetDateTime registrationDeadline, String registrationUrl,
        String responseSheetUrl, boolean googleIntegrationAvailable
) { }
