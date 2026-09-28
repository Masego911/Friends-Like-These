package com.friendslikethese.backend.registration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.google-sheets")
public record GoogleSheetsProperties(String sheetId, String sheetName) {
    public boolean isConfigured() {
        return sheetId != null && !sheetId.isBlank() && sheetName != null && !sheetName.isBlank();
    }

    public String responseSheetUrl() {
        return isConfigured() ? "https://docs.google.com/spreadsheets/d/" + sheetId + "/edit" : null;
    }
}
