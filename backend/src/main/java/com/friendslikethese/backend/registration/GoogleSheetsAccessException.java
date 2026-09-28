package com.friendslikethese.backend.registration;

final class GoogleSheetsAccessException extends RuntimeException {
    private final GoogleSheetsFailureCategory category;
    private final Integer httpStatus;
    private final String safeMessage;

    GoogleSheetsAccessException(GoogleSheetsFailureCategory category, Integer httpStatus,
                                String safeMessage, Throwable cause) {
        super(safeMessage, cause);
        this.category = category;
        this.httpStatus = httpStatus;
        this.safeMessage = safeMessage;
    }

    GoogleSheetsFailureCategory category() { return category; }
    Integer httpStatus() { return httpStatus; }
    String safeMessage() { return safeMessage; }
}
