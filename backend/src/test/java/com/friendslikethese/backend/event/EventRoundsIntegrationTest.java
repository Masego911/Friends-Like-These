package com.friendslikethese.backend.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventRoundsIntegrationTest {
    @Autowired EventService eventService;
    @Autowired EventRepository eventRepository;
    @Autowired MockMvc mvc;

    @Test
    void totalRoundsPersistsAndCurrentRoundDefaultsToOne() {
        EventResponse response = eventService.updateSettings(new UpdateEventSettingsRequest(null, 7));
        Event saved = eventRepository.findById(response.id()).orElseThrow();
        assertEquals(7, saved.getTotalRounds());
        assertEquals(1, saved.getCurrentRound());
        assertEquals(7, eventService.getCurrentEvent().totalRounds());
        assertEquals(1, eventService.getCurrentEvent().currentRound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidRoundValuesAreRejectedByEntityAndApiValidation() throws Exception {
        Event event = new Event("Test", null, EventStatus.DRAFT);
        assertThrows(IllegalArgumentException.class, () -> event.updateTotalRounds(0));
        assertThrows(IllegalArgumentException.class, () -> event.updateTotalRounds(51));

        mvc.perform(patch("/api/events/current/settings").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"totalRounds\":0}"))
                .andExpect(status().isBadRequest());
    }
}
