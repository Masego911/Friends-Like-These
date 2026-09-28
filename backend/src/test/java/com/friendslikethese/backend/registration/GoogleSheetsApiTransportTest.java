package com.friendslikethese.backend.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleSheetsApiTransportTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void readsValuesWithBearerAuthentication() {
        RestClient.Builder builder = RestClient.builder();
                MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), request -> {
                    String uri = URLDecoder.decode(request.getURI().toString(), StandardCharsets.UTF_8);
                    if (!uri.contains("/v4/spreadsheets/sheet/values/") || !uri.contains("A:S")) {
                        throw new AssertionError("Unexpected Sheets API URI");
                    }
                })
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess("{\"values\":[[\"Timestamp\",\"Email Address\",\"Team Name\"]]}",
                        MediaType.APPLICATION_JSON));
        GoogleSheetsApiTransport transport = transport(builder, this::credentials);

        var response = transport.readValues("sheet", "'Form Responses 1'!A:S");

        assertEquals("Team Name", response.path("values").get(0).get(2).asText());
        server.verify();
    }

    @Test
    void missingCredentialsAreCategorized() {
        GoogleSheetsApiTransport transport = transport(RestClient.builder(), () -> {
            throw new IOException("The Application Default Credentials are not available");
        });

        GoogleSheetsAccessException exception = assertThrows(GoogleSheetsAccessException.class,
                () -> transport.readValues("sheet", "'Form Responses 1'!A:S"));

        assertEquals(GoogleSheetsFailureCategory.CREDENTIALS_NOT_FOUND, exception.category());
    }

    @Test
    void authenticationFailureIsCategorized() {
        assertFailure(HttpStatus.UNAUTHORIZED, "{\"error\":{\"message\":\"Invalid Credentials\"}}",
                GoogleSheetsFailureCategory.AUTHENTICATION_FAILED);
    }

    @Test
    void permissionDeniedIsCategorized() {
        assertFailure(HttpStatus.FORBIDDEN,
                "{\"error\":{\"message\":\"The caller does not have permission\",\"errors\":[{\"reason\":\"forbidden\"}]}}",
                GoogleSheetsFailureCategory.PERMISSION_DENIED);
    }

    @Test
    void disabledApiIsDistinguishedFromSheetPermission() {
        assertFailure(HttpStatus.FORBIDDEN,
                "{\"error\":{\"message\":\"Google Sheets API has not been used or is disabled\",\"errors\":[{\"reason\":\"accessNotConfigured\"}]}}",
                GoogleSheetsFailureCategory.API_DISABLED);
    }

    @Test
    void missingSpreadsheetIsCategorized() {
        assertFailure(HttpStatus.NOT_FOUND, "{\"error\":{\"message\":\"Requested entity was not found\"}}",
                GoogleSheetsFailureCategory.SPREADSHEET_NOT_FOUND);
    }

    @Test
    void invalidRangeIsCategorized() {
        assertFailure(HttpStatus.BAD_REQUEST, "{\"error\":{\"message\":\"Unable to parse range\"}}",
                GoogleSheetsFailureCategory.RANGE_NOT_FOUND);
    }

    @Test
    void temporaryFailureIsCategorized() {
        assertFailure(HttpStatus.SERVICE_UNAVAILABLE, "{\"error\":{\"message\":\"Backend error\"}}",
                GoogleSheetsFailureCategory.GOOGLE_UNAVAILABLE);
    }

    private void assertFailure(HttpStatus status, String body, GoogleSheetsFailureCategory expected) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), request -> { })
                .andRespond(withStatus(status).contentType(MediaType.APPLICATION_JSON).body(body));
        GoogleSheetsApiTransport transport = transport(builder, this::credentials);

        GoogleSheetsAccessException exception = assertThrows(GoogleSheetsAccessException.class,
                () -> transport.readValues("sheet", "'Form Responses 1'!A:S"));

        assertEquals(expected, exception.category());
        assertEquals(status.value(), exception.httpStatus());
        server.verify();
    }

    private GoogleSheetsApiTransport transport(RestClient.Builder builder, GoogleCredentialsProvider provider) {
        return new GoogleSheetsApiTransport(builder.baseUrl("https://sheets.googleapis.com").build(), mapper, provider);
    }

    private GoogleCredentials credentials() {
        return GoogleCredentials.create(new AccessToken("test-token", Date.from(Instant.now().plusSeconds(3600))));
    }
}
