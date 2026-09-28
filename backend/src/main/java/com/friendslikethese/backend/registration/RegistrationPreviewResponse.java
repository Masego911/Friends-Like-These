package com.friendslikethese.backend.registration;
import java.util.List;
public record RegistrationPreviewResponse(int rowsRead, int validRegistrations, int newTeams, int existingTeams, int conflicts, int skippedRows, List<RegistrationPreview> registrations) { }
