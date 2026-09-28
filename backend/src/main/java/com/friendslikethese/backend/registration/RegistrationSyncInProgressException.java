package com.friendslikethese.backend.registration;

public class RegistrationSyncInProgressException extends RuntimeException {
    public RegistrationSyncInProgressException() {
        super("Registration synchronization is already in progress.");
    }
}
