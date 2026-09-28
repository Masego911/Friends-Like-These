package com.friendslikethese.backend.registration;

public class RegistrationImportException extends RuntimeException {
    public RegistrationImportException(String message) { super(message); }
    public RegistrationImportException(String message, Throwable cause) { super(message, cause); }
}
