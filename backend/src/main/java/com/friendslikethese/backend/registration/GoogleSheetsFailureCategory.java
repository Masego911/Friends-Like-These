package com.friendslikethese.backend.registration;

enum GoogleSheetsFailureCategory {
    CREDENTIALS_NOT_FOUND,
    AUTHENTICATION_FAILED,
    PERMISSION_DENIED,
    SPREADSHEET_NOT_FOUND,
    RANGE_NOT_FOUND,
    API_DISABLED,
    GOOGLE_UNAVAILABLE
}
