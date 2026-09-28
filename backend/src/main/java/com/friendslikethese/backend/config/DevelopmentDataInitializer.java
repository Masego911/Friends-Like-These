package com.friendslikethese.backend.config;

import com.friendslikethese.backend.event.Event;
import com.friendslikethese.backend.event.EventRepository;
import com.friendslikethese.backend.event.EventStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.OffsetDateTime;

@Configuration
@Profile({"dev", "test"})
public class DevelopmentDataInitializer {

    @Bean
    CommandLineRunner initialiseEvent(EventRepository eventRepository) {
        return args -> {
            if (eventRepository.count() == 0) {
                Event event = new Event(
                        "Friends Like These",
                        OffsetDateTime.parse("2026-09-30T20:00:00+02:00"),
                        EventStatus.REGISTRATION_OPEN
                );
                event.markCurrent();

                eventRepository.save(event);
            }
        };
    }
}
