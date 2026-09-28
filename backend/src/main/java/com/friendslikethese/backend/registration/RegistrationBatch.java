package com.friendslikethese.backend.registration;
import java.util.List;
public record RegistrationBatch(int rowsRead, int rowsSkipped, List<Registration> registrations) { }
