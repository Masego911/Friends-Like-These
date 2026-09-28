package com.friendslikethese.backend.registration;

import com.friendslikethese.backend.team.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class RegistrationImportIntegrationTest {
    @MockitoBean GoogleSheetsClient sheetsClient;
    @Autowired RegistrationImportService importService;
    @Autowired TeamRepository teamRepository;

    @BeforeEach
    void removeTeams() {
        teamRepository.deleteAll();
    }

    @Test
    void importingExactlyTheSameSheetTwiceIsIdempotentAndPreservesScore() {
        var batch = new RegistrationBatch(1, 0,
                List.of(new Registration("  Quiz Masters  ", List.of(" Alice ", "Bob"))));
        when(sheetsClient.fetchRegistrations()).thenReturn(batch);

        RegistrationImportResult first = importService.importRegistrations();
        var team = teamRepository.findAll().get(0);
        team.applyScore(37);
        teamRepository.saveAndFlush(team);

        RegistrationImportResult second = importService.importRegistrations();
        var saved = teamRepository.findAll();

        assertEquals(1, first.teamsCreated());
        assertEquals(0, second.teamsCreated());
        assertEquals(1, second.teamsUnchanged());
        assertEquals(1, saved.size());
        assertEquals("Quiz Masters", saved.get(0).getName());
        assertEquals(37, saved.get(0).getScore());
        assertEquals(List.of("Alice", "Bob"),
                saved.get(0).getMembers().stream().map(member -> member.getFullName()).toList());
    }

    @Test
    void caseWhitespaceAndMemberChangesUpdateOneExistingTeamWithoutChangingScore() {
        when(sheetsClient.fetchRegistrations()).thenReturn(
                new RegistrationBatch(1, 0, List.of(new Registration("The Winners", List.of("A", "B")))),
                new RegistrationBatch(1, 0, List.of(new Registration("  the winners ", List.of("A", "C")))));

        importService.importRegistrations();
        var team = teamRepository.findAll().get(0);
        team.applyScore(12);
        teamRepository.saveAndFlush(team);

        RegistrationImportResult second = importService.importRegistrations();
        var saved = teamRepository.findAll();

        assertEquals(1, second.teamsUpdated());
        assertEquals(0, second.teamsCreated());
        assertEquals(1, saved.size());
        assertEquals(12, saved.get(0).getScore());
        assertEquals(List.of("A", "C"),
                saved.get(0).getMembers().stream().map(member -> member.getFullName()).toList());
    }

    @Test
    void registrationMatchingSoftDeletedTeamRestoresItWithoutViolatingUniqueConstraint() {
        when(sheetsClient.fetchRegistrations()).thenReturn(
                new RegistrationBatch(1, 0, List.of(new Registration("The All-Stars", List.of("A", "B")))));
        importService.importRegistrations();
        var team = teamRepository.findAll().get(0);
        team.applyScore(19);
        team.softDelete();
        teamRepository.saveAndFlush(team);

        RegistrationImportResult result = importService.importRegistrations();
        var saved = teamRepository.findAll();

        assertEquals(0, result.teamsCreated());
        assertEquals(1, result.teamsUpdated());
        assertEquals(1, saved.size());
        assertFalse(saved.get(0).isDeleted());
        assertEquals(19, saved.get(0).getScore());
    }
}
