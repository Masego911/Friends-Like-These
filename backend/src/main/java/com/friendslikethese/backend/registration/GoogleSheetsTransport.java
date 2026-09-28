package com.friendslikethese.backend.registration;

import com.fasterxml.jackson.databind.JsonNode;

interface GoogleSheetsTransport {
    JsonNode readValues(String spreadsheetId, String range);
}
