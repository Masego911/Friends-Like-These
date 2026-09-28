package com.friendslikethese.backend.registration;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleSheetsClient {
    private static final Logger log = LoggerFactory.getLogger(GoogleSheetsClient.class);
    static final String SHEET_RANGE = "A:S";
    static final int TEAM_NAME_COLUMN = 2;
    private static final int[] MEMBER_COLUMNS = {3, 6, 9, 12, 15, 18};
    private final GoogleSheetsProperties properties;
    private final GoogleSheetsTransport transport;

    public GoogleSheetsClient(GoogleSheetsProperties properties, GoogleSheetsTransport transport) {
        this.properties = properties;
        this.transport = transport;
    }

    public RegistrationBatch fetchRegistrations() {
        if (!properties.isConfigured()) {
            throw new RegistrationImportException("Google Sheets registration integration is not configured.");
        }
        try {
            String range = "'" + properties.sheetName().replace("'", "''") + "'!" + SHEET_RANGE;
            JsonNode root = transport.readValues(properties.sheetId(), range);
            RegistrationBatch batch = parseValues(root == null ? null : root.get("values"));
            log.info("Google registration sheet read completed: worksheet={}, range={}, rowsRead={}",
                    properties.sheetName(), SHEET_RANGE, batch.rowsRead());
            return batch;
        } catch (RegistrationImportException exception) {
            throw exception;
        } catch (GoogleSheetsAccessException exception) {
            log.warn("Google Sheets request failed: category={}, status={}, message={}",
                    exception.category(), exception.httpStatus(), exception.safeMessage());
            throw new RegistrationImportException("Google Sheets integration is unavailable.", exception);
        } catch (RestClientException | IllegalArgumentException exception) {
            throw new RegistrationImportException("Google Sheets is temporarily unavailable.", exception);
        }
    }

    RegistrationBatch parseValues(JsonNode values) {
            List<Registration> result = new ArrayList<>();
            if (values == null || !values.isArray() || values.isEmpty()) {
                throw new RegistrationImportException("Google Sheets response does not contain the expected header row.");
            }
            JsonNode header = values.get(0);
            if (!header.isArray() || header.size() <= MEMBER_COLUMNS[MEMBER_COLUMNS.length - 1]
                    || !isTeamNameHeader(cell(header, TEAM_NAME_COLUMN))) {
                throw new RegistrationImportException("Google Sheets response headers do not match the configured registration layout.");
            }
            int rowsRead = values.size() > 0 ? values.size() - 1 : 0;
            int skipped = 0;
            // Google Sheets values include the headings in row zero.  Never parse it as data.
            for (int rowIndex = 1; rowIndex < values.size(); rowIndex++) {
                JsonNode row = values.get(rowIndex);
                String team = cell(row, TEAM_NAME_COLUMN);
                if (team.isBlank() || isTeamNameHeader(team)) { skipped++; continue; }
                List<String> members = new ArrayList<>();
                for (int column : MEMBER_COLUMNS) { String member = cell(row, column); if (!member.isBlank()) members.add(member); }
                result.add(new Registration(team, members, parseSubmittedAt(cell(row, 0))));
            }
            return new RegistrationBatch(rowsRead, skipped, result);
    }

    static boolean isTeamNameHeader(String value) {
        return value != null && value.trim().equalsIgnoreCase("Team Name");
    }

    private OffsetDateTime parseSubmittedAt(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("Timestamp")) return null;
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            // Google Forms commonly renders timestamps without an offset in the Sheet locale.
        }
        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss"),
                DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss"))) {
            try {
                return LocalDateTime.parse(value, formatter)
                        .atZone(ZoneId.systemDefault()).toOffsetDateTime();
            } catch (DateTimeParseException ignored) {
                // Try the next known Google Forms timestamp format.
            }
        }
        return null;
    }

    private String cell(JsonNode row, int index) {
        return row.isArray() && row.size() > index && !row.get(index).isNull() ? row.get(index).asText().trim() : "";
    }
}
