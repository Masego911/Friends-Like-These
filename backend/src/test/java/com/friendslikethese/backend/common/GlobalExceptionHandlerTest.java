package com.friendslikethese.backend.common;

import com.friendslikethese.backend.registration.RegistrationImportException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {
    @Test
    void registrationErrorResponseDoesNotExposeCredentialCause() {
        String sensitiveCause = "private credential detail";
        var exception = new RegistrationImportException("Google Sheets integration is unavailable.",
                new IllegalStateException(sensitiveCause));

        var response = new GlobalExceptionHandler().handleRegistrationImport(exception);

        assertEquals(502, response.getStatusCode().value());
        assertEquals("Google Sheets integration is unavailable.", response.getBody().message());
        assertFalse(response.getBody().message().contains(sensitiveCause));
    }
}
