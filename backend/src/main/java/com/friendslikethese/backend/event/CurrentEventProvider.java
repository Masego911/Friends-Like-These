package com.friendslikethese.backend.event;

import com.friendslikethese.backend.common.BusinessRuleException;
import com.friendslikethese.backend.common.ResourceNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CurrentEventProvider {
    private final EventRepository events;
    public CurrentEventProvider(EventRepository events) { this.events = events; }

    public Event requireCurrent() {
        return events.findByCurrentTrue().orElseThrow(() -> new ResourceNotFoundException("No current game has been configured."));
    }

    public Event requireMutable() {
        Event event = events.findCurrentForUpdate()
                .orElseThrow(() -> new ResourceNotFoundException("No current game has been configured."));
        if (event.getStatus() == EventStatus.COMPLETED || event.getStatus() == EventStatus.ARCHIVED) {
            throw new BusinessRuleException("Completed or archived games are read-only.");
        }
        return event;
    }

    public Event requireLive() {
        Event event = requireMutable();
        if (event.getStatus() != EventStatus.LIVE) throw new BusinessRuleException("This operation is only available while the game is LIVE.");
        return event;
    }

    public Event requireRegistrationOpen() {
        Event event = requireMutable();
        if (event.getStatus() != EventStatus.REGISTRATION_OPEN) throw new BusinessRuleException("Registrations are not open for the current game.");
        return event;
    }
}
