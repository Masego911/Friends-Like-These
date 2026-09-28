package com.friendslikethese.backend.registration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.registration.sync")
public record RegistrationSyncProperties(
        boolean enabled,
        Duration interval,
        Duration closeGracePeriod
) {
    public RegistrationSyncProperties {
        interval = interval == null || interval.isNegative() || interval.isZero() ? Duration.ofSeconds(60) : interval;
        closeGracePeriod = closeGracePeriod == null || closeGracePeriod.isNegative()
                ? Duration.ofMinutes(2) : closeGracePeriod;
    }
}
