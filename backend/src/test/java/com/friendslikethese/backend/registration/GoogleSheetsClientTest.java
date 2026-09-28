package com.friendslikethese.backend.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleSheetsClientTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final GoogleSheetsProperties properties = new GoogleSheetsProperties("sheet", "Form Responses 1");

    @Test
    void skipsFirstRowAndDefensivelyRejectsHeaderAndBlankTeamNames() {
        ArrayNode values = mapper.createArrayNode();
        values.add(row("Team Name", "Member 1", "Member 2"));
        values.add(row("  Alpha Team  ", " Alice ", "Bob"));
        values.add(row(" team name ", "Should Not Import", ""));
        values.add(row("   ", "Nobody", ""));

        RegistrationBatch batch = clientReturning(values).fetchRegistrations();

        assertEquals(3, batch.rowsRead());
        assertEquals(2, batch.rowsSkipped());
        assertEquals(1, batch.registrations().size());
        assertEquals("Alpha Team", batch.registrations().get(0).teamName());
        assertEquals(java.util.List.of("Alice", "Bob"), batch.registrations().get(0).members());
        assertTrue(batch.registrations().stream().noneMatch(r -> r.teamName().equalsIgnoreCase("Team Name")));
    }

    @Test
    void headerOnlySheetIsASuccessfulReadWithNoRegistrations() {
        ArrayNode values = mapper.createArrayNode();
        values.add(row("Team Name", "Team Leader Full Names", "Member 1 Full Names"));

        RegistrationBatch batch = clientReturning(values).fetchRegistrations();

        assertEquals(0, batch.rowsRead());
        assertTrue(batch.registrations().isEmpty());
    }

    @Test
    void missingOrUnexpectedHeaderIsReportedAsAConfigurationLayoutFailure() {
        assertThrows(RegistrationImportException.class,
                () -> clientReturning(mapper.createArrayNode()).fetchRegistrations());

        ArrayNode values = mapper.createArrayNode();
        values.add(row("Wrong heading", "Member 1", "Member 2"));
        RegistrationImportException exception = assertThrows(
                RegistrationImportException.class, () -> clientReturning(values).fetchRegistrations());

        assertTrue(exception.getMessage().contains("headers do not match"));
    }

    @Test
    void successfulReadUsesConfiguredSpreadsheetAndQuotedRange() {
        ArrayNode values = mapper.createArrayNode();
        values.add(row("Team Name", "Team Leader Full Names", "Member 1 Full Names"));
        values.add(row("Alpha Team", "Alice", "Bob"));
        String[] requested = new String[2];
        GoogleSheetsClient client = new GoogleSheetsClient(properties, (spreadsheetId, range) -> {
            requested[0] = spreadsheetId;
            requested[1] = range;
            return mapper.createObjectNode().set("values", values);
        });

        RegistrationBatch batch = client.fetchRegistrations();

        assertEquals("sheet", requested[0]);
        assertEquals("'Form Responses 1'!A:S", requested[1]);
        assertEquals(1, batch.rowsRead());
        assertEquals(1, batch.registrations().size());
    }

    @Test
    void permissionFailureHasControlledMessage() {
        GoogleSheetsClient client = new GoogleSheetsClient(properties, (spreadsheetId, range) -> {
            throw new GoogleSheetsAccessException(GoogleSheetsFailureCategory.PERMISSION_DENIED, 403,
                    "The service account does not have permission to read the spreadsheet.", null);
        });

        RegistrationImportException exception = assertThrows(
                RegistrationImportException.class, client::fetchRegistrations);

        assertEquals("Google Sheets integration is unavailable.", exception.getMessage());
        assertTrue(exception.getCause() instanceof GoogleSheetsAccessException);
    }

    @Test
    void temporaryGoogleFailureHasControlledMessage() {
        GoogleSheetsClient client = new GoogleSheetsClient(properties, (spreadsheetId, range) -> {
            throw new GoogleSheetsAccessException(GoogleSheetsFailureCategory.GOOGLE_UNAVAILABLE, 503,
                    "Google Sheets is temporarily unavailable.", null);
        });

        RegistrationImportException exception = assertThrows(
                RegistrationImportException.class, client::fetchRegistrations);

        assertEquals("Google Sheets integration is unavailable.", exception.getMessage());
    }

    private GoogleSheetsClient clientReturning(ArrayNode values) {
        return new GoogleSheetsClient(properties,
                (spreadsheetId, range) -> mapper.createObjectNode().set("values", values));
    }

    private ArrayNode row(String teamName, String firstMember, String secondMember) {
        ArrayNode row = mapper.createArrayNode();
        for (int index = 0; index <= 18; index++) row.add("");
        row.set(2, mapper.getNodeFactory().textNode(teamName));
        row.set(3, mapper.getNodeFactory().textNode(firstMember));
        row.set(6, mapper.getNodeFactory().textNode(secondMember));
        return row;
    }
}
