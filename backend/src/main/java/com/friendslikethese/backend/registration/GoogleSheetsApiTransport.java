package com.friendslikethese.backend.registration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Component
final class GoogleSheetsApiTransport implements GoogleSheetsTransport {
    static final String READ_ONLY_SCOPE = "https://www.googleapis.com/auth/spreadsheets.readonly";

    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final GoogleCredentialsProvider credentialsProvider;
    private GoogleCredentials credentials;

    @Autowired
    GoogleSheetsApiTransport(RestClient.Builder builder, ObjectMapper objectMapper) {
        this(configuredClient(builder), objectMapper, GoogleCredentials::getApplicationDefault);
    }

    GoogleSheetsApiTransport(RestClient client, ObjectMapper objectMapper,
                             GoogleCredentialsProvider credentialsProvider) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.credentialsProvider = credentialsProvider;
    }

    private static RestClient configuredClient(RestClient.Builder builder) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return builder.requestFactory(factory).baseUrl("https://sheets.googleapis.com").build();
    }

    @Override
    public JsonNode readValues(String spreadsheetId, String range) {
        try {
            return client.get()
                    .uri(uri -> uri.path("/v4/spreadsheets/{spreadsheetId}/values/{range}")
                            .build(spreadsheetId, range))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            throw classify(exception);
        }
    }

    private synchronized String accessToken() {
        try {
            if (credentials == null) {
                credentials = credentialsProvider.load()
                        .createScoped(List.of(READ_ONLY_SCOPE));
            }
            credentials.refreshIfExpired();
            AccessToken token = credentials.getAccessToken();
            if (token == null || token.getTokenValue() == null || token.getTokenValue().isBlank()) {
                throw new GoogleSheetsAccessException(GoogleSheetsFailureCategory.AUTHENTICATION_FAILED, null,
                        "Application Default Credentials did not provide an access token.", null);
            }
            return token.getTokenValue();
        } catch (IOException exception) {
            String detail = exception.getMessage() == null ? "" : exception.getMessage().toLowerCase(Locale.ROOT);
            GoogleSheetsFailureCategory category = detail.contains("default credentials")
                    || detail.contains("environment variable") || detail.contains("file does not exist")
                    ? GoogleSheetsFailureCategory.CREDENTIALS_NOT_FOUND
                    : GoogleSheetsFailureCategory.AUTHENTICATION_FAILED;
            throw new GoogleSheetsAccessException(category, null,
                    category == GoogleSheetsFailureCategory.CREDENTIALS_NOT_FOUND
                            ? "Application Default Credentials were not found."
                            : "Application Default Credentials could not be used.", exception);
        }
    }

    private GoogleSheetsAccessException classify(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        String googleReason = "";
        String googleMessage = "";
        try {
            JsonNode error = objectMapper.readTree(exception.getResponseBodyAsString()).path("error");
            googleMessage = error.path("message").asText("");
            JsonNode errors = error.path("errors");
            if (errors.isArray() && !errors.isEmpty()) googleReason = errors.get(0).path("reason").asText("");
        } catch (Exception ignored) {
            // Classification falls back to HTTP status without logging the response body.
        }
        String signal = (googleReason + " " + googleMessage).toLowerCase(Locale.ROOT);
        GoogleSheetsFailureCategory category;
        String message;
        if (status == 401) {
            category = GoogleSheetsFailureCategory.AUTHENTICATION_FAILED;
            message = "Google rejected the supplied access token.";
        } else if (status == 403 && (signal.contains("accessnotconfigured")
                || signal.contains("api has not been used") || signal.contains("api is disabled"))) {
            category = GoogleSheetsFailureCategory.API_DISABLED;
            message = "The Google Sheets API is not enabled for the credential project.";
        } else if (status == 403) {
            category = GoogleSheetsFailureCategory.PERMISSION_DENIED;
            message = "The service account does not have permission to read the spreadsheet.";
        } else if (status == 404) {
            category = GoogleSheetsFailureCategory.SPREADSHEET_NOT_FOUND;
            message = "The configured spreadsheet was not found.";
        } else if (status == 400 && (signal.contains("unable to parse range")
                || signal.contains("range") || signal.contains("sheet"))) {
            category = GoogleSheetsFailureCategory.RANGE_NOT_FOUND;
            message = "The configured worksheet or range was not found.";
        } else {
            category = GoogleSheetsFailureCategory.GOOGLE_UNAVAILABLE;
            message = "Google Sheets is temporarily unavailable.";
        }
        return new GoogleSheetsAccessException(category, status, message, exception);
    }
}
