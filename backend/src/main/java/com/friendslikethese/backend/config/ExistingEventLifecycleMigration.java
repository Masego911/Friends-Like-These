package com.friendslikethese.backend.config;

import com.friendslikethese.backend.event.EventStatus;
import com.friendslikethese.backend.event.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class ExistingEventLifecycleMigration {
    @Bean
    CommandLineRunner assignExistingCurrentEvent(EventRepository events) {
        return args -> migrate(events);
    }

    @Transactional
    void migrate(EventRepository events) {
        if (events.existsByCurrentTrue()) return;
        events.findFirstByOrderByCreatedAtDesc()
                .filter(event -> event.getStatus() != EventStatus.COMPLETED && event.getStatus() != EventStatus.ARCHIVED)
                .ifPresent(event -> { event.markCurrent(); events.save(event); });
    }
}
