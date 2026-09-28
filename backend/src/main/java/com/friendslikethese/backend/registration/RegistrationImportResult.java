package com.friendslikethese.backend.registration;

public record RegistrationImportResult(int rowsRead, int teamsCreated, int teamsUpdated, int teamsUnchanged, int rowsSkipped, int conflicts) {
    public RegistrationImportResult(int rowsRead, int teamsCreated, int teamsUpdated, int teamsUnchanged, int rowsSkipped) {
        this(rowsRead, teamsCreated, teamsUpdated, teamsUnchanged, rowsSkipped, 0);
    }
}
